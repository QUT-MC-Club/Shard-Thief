package io.github.haykam821.shardthief.game.phase;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import eu.pb4.holograms.api.holograms.AbstractHologram;
import io.github.haykam821.shardthief.game.DroppedShard;
import io.github.haykam821.shardthief.game.PlayerShardEntry;
import io.github.haykam821.shardthief.game.ShardInventoryManager;
import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.ShardThiefCountBar;
import io.github.haykam821.shardthief.game.map.ShardThiefMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class ShardThiefActivePhase {
	private final ServerWorld world;
	private final GameSpace gameSpace;
	private final ShardThiefMap map;
	private final ShardThiefConfig config;
	private final Set<PlayerShardEntry> players;
	private final ShardThiefCountBar countBar;

	private final AbstractHologram guideText;
	private int guideTicks;

	private PlayerShardEntry shardHolder;
	private int ticksUntilCount;
	private int ticksUntilKitRestock;
	private DroppedShard droppedShard;

	public ShardThiefActivePhase(GameSpace gameSpace, ServerWorld world, ShardThiefMap map, ShardThiefConfig config, Set<ServerPlayerEntity> players, GlobalWidgets widgets, AbstractHologram guideText) {
		this.world = world;
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;

		this.players = players.stream().map(player -> {
			return new PlayerShardEntry(player, this.config.getStartingCounts(), this.config.getShardInvulnerability());
		}).collect(Collectors.toSet());

		this.countBar = new ShardThiefCountBar(gameSpace.getMetadata().sourceConfig().name(), widgets);

		this.placeShard(this.map.getCenterSpawnPos().down());

		this.guideText = guideText;
		this.guideTicks = this.config.getGuideTicks();
	}

	public static void setRules(GameActivity activity, boolean pvp) {
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.ICE_MELT);
		activity.deny(GameRuleType.INTERACTION);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.THROW_ITEMS);

		if (pvp) {
			activity.allow(GameRuleType.PVP);

			activity.allow(GameRuleType.INTERACTION);
			activity.deny(GameRuleType.USE_BLOCKS);
			activity.deny(GameRuleType.USE_ENTITIES);
		} else {
			activity.deny(GameRuleType.PVP);
		}
	}

	public static void open(GameSpace gameSpace, ServerWorld world, ShardThiefMap map, ShardThiefConfig config, AbstractHologram guideText) {
		gameSpace.setActivity(activity -> {
			GlobalWidgets widgets = GlobalWidgets.addTo(activity);

			ShardThiefActivePhase active = new ShardThiefActivePhase(gameSpace, world, map, config, Sets.newHashSet(gameSpace.getPlayers()), widgets, guideText);
			ShardThiefActivePhase.setRules(activity, true);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, active::enable);
			activity.listen(GameActivityEvents.TICK, active::tick);
			activity.listen(GamePlayerEvents.OFFER, active::offerPlayer);
			activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
			activity.listen(GamePlayerEvents.REMOVE, active::removePlayer);
		});
	}

	private void enable() {
		int index = 0;
		for (PlayerShardEntry entry : this.players) {
			ServerPlayerEntity player = entry.getPlayer();

			player.changeGameMode(GameMode.ADVENTURE);
			ShardInventoryManager.giveNonShardInventory(player);

			ShardThiefActivePhase.spawn(this.world, this.map, player, index);
			index += 1;
		}
	}

	public float getTimerBarPercent() {
		if (this.shardHolder == null) {
			return 1;
		}
		return this.shardHolder.getCounts() / (float) this.config.getStartingCounts();
	}

	private void resetTicksUntilCount() {
		this.ticksUntilCount = this.config.getCountDuration();
	}

	private void clearShard() {
		if (this.shardHolder == null) return;

		this.shardHolder.getPlayer().getInventory().clear();
		ShardInventoryManager.giveNonShardInventory(this.shardHolder.getPlayer());

		if (this.shardHolder.getCounts() < this.config.getRestartCounts()) {
			this.shardHolder.setCounts(this.config.getRestartCounts());
		}
		this.shardHolder = null;

		this.resetTicksUntilCount();
	}

	private void setShardHolder(PlayerShardEntry entry) {
		this.clearShard();
		this.shardHolder = entry;
		entry.setInvulnerability(this.config.getShardInvulnerability());

		entry.getPlayer().getInventory().clear();
		ShardInventoryManager.giveShardInventory(entry.getPlayer());
	}

	private void sendStealMessage() {
		Text stealText = this.shardHolder.getStealMessage();
		this.gameSpace.getPlayers().sendActionBar(stealText);
	}

	private void pickUpShard(PlayerShardEntry entry) {
		this.setShardHolder(entry);

		this.droppedShard.reset(this.world);
		this.droppedShard = null;

		this.applyStealSpeed(entry.getPlayer());
	
		this.world.playSound(null, entry.getPlayer().getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
		this.sendStealMessage();
	}

	private BlockPos findDropPos(BlockPos initialPos) {
		BlockPos.Mutable pos = initialPos.mutableCopy();
		while (true) {
			if (pos.getY() == 0) return pos;

			BlockState state = this.world.getBlockState(pos);
			if (DroppedShard.isDroppableOn(state, this.world, pos)) {
				return pos;
			}

			pos.move(Direction.DOWN);
		}
	}

	public void attemptResetShard(int ticks, BlockPos pos) {
		if (ticks < this.config.getDroppedShardResetTicks()) return;
		if (pos.equals(this.map.getCenterSpawnPos().down())) return;

		this.droppedShard.reset(this.world);

		this.placeShard(this.map.getCenterSpawnPos().down());
		this.playDropSound(pos);

		Text message = this.droppedShard.getResetMessage();
		this.gameSpace.getPlayers().sendMessage(message);
	}

	private void placeShard(BlockPos pos) {
		this.droppedShard = new DroppedShard(this, pos, this.world.getBlockState(pos), this.config.getShardInvulnerability());
		this.droppedShard.place(this.world);
	}

	private void playDropSound(BlockPos pos) {
		world.playSound(null, pos, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.PLAYERS, 1, 1);
	}

	private void dropShard() {
		BlockPos pos = this.findDropPos(this.shardHolder.getPlayer().getBlockPos());
		this.placeShard(pos);

		this.clearShard();

		this.playDropSound(pos);
	}

	private Formatting getCountTitleColor() {
		int counts = this.shardHolder.getCounts();
		if (counts <= 1) {
			return Formatting.RED;
		} else if (counts <= 3) {
			return Formatting.GOLD;
		} else {
			return Formatting.YELLOW;
		}
	}

	private void tickCounts() {
		this.shardHolder.decrementCounts();
		if (this.shardHolder.getCounts() <= 0) {
			Text message = this.shardHolder.getWinMessage();
			this.gameSpace.getPlayers().sendMessage(message);

			this.gameSpace.getPlayers().playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1, 1);

			this.gameSpace.close(GameCloseReason.FINISHED);
			return;
		} else if (this.shardHolder.getCounts() <= 5) {
			String countString = Integer.toString(this.shardHolder.getCounts());
			Text countText = Text.literal(countString).formatted(this.getCountTitleColor()).formatted(Formatting.BOLD);
			this.gameSpace.getPlayers().showTitle(countText, 70);

			this.gameSpace.getPlayers().playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.PLAYERS, 1, 1.5f);
		}

		this.resetTicksUntilCount();
	}

	private boolean canPlayerPickUpDroppedShard(PlayerEntity player) {
		return this.droppedShard != null && this.droppedShard.canPlayerPickUp(player);
	}

	private void restockKits() {
		for (PlayerShardEntry entry : this.players) {
			if (!entry.equals(this.shardHolder)) {
				ShardInventoryManager.restockArrows(entry.getPlayer(), this.config.getMaxArrows());
			}
		}
		this.ticksUntilKitRestock = this.config.getKitRestockInterval();
	}

	private void tick() {
		this.countBar.tick(this);

		// Remove guide text after guide ticks reach zero
		if (this.guideTicks > 0) {
			this.guideTicks -= 1;
		} else if (this.guideText != null && this.guideTicks == 0) {
			this.guideText.hide();
		}

		if (this.droppedShard != null) {
			this.droppedShard.tick();
		}

		if (this.ticksUntilKitRestock <= 0) {
			this.restockKits();
		}
		this.ticksUntilKitRestock -= 1;
	
		if (this.shardHolder != null) {
			if (this.ticksUntilCount <= 0) {
				this.tickCounts();
			}
			this.ticksUntilCount -= 1;
		}

		for (PlayerShardEntry entry : this.players) {
			entry.tick();

			ServerPlayerEntity player = entry.getPlayer();
			ShardThiefActivePhase.respawnIfOutOfBounds(player, this.map, this.world);

			if (!entry.equals(this.shardHolder) && this.canPlayerPickUpDroppedShard(player)) {
				this.pickUpShard(entry);
			}
		}
	}

	private void setSpectator(ServerPlayerEntity player) {
		player.changeGameMode(GameMode.SPECTATOR);
	}

	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, Vec3d.of(this.map.getCenterSpawnPos())).and(() -> {
			this.setSpectator(offer.player());
		});
	}

	private void removePlayer(ServerPlayerEntity player) {
		// Drop shard when player is removed
		if (this.shardHolder != null && player.equals(this.shardHolder.getPlayer())) {
			this.dropShard();
		}

		this.players.removeIf(entry -> {
			return player.equals(entry.getPlayer());
		});
	}

	private void applyStealSpeed(ServerPlayerEntity player) {
		if (this.config.getSpeedAmplifier() <= 0) return;
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, this.config.getShardInvulnerability() * 2, this.config.getSpeedAmplifier(), true, false, true));
	}

	private void tryTransferShard(ServerPlayerEntity damagedPlayer, DamageSource source) {
		if (!(source.getAttacker() instanceof ServerPlayerEntity)) return;
		ServerPlayerEntity attacker = (ServerPlayerEntity) source.getAttacker();

		if (this.shardHolder == null) return;

		PlayerEntity shardPlayer = this.shardHolder.getPlayer();
		if (!damagedPlayer.equals(shardPlayer)) return;
		if (attacker.equals(shardPlayer)) return;

		for (PlayerShardEntry entry : this.players) {
			if (attacker.equals(entry.getPlayer())) {
				if (source.isProjectile()) {
					this.dropShard();
					if (source.getSource() instanceof ProjectileEntity) {
						source.getSource().kill();
					}
 				} else if (this.shardHolder.canBeStolen()) {
					this.setShardHolder(entry);
					this.applyStealSpeed(entry.getPlayer());
					this.sendStealMessage();
				}
				return;
			}
		}
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity damagedPlayer, DamageSource source, float damage) {
		this.tryTransferShard(damagedPlayer, source);
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		ShardThiefActivePhase.spawn(this.world, this.map, player, 0);
		return ActionResult.SUCCESS;
	}

	public static void spawn(ServerWorld world, ShardThiefMap map, ServerPlayerEntity player, int index) {
		Direction direction = Direction.fromHorizontal(index);
		int distance = (int) Math.min(index / 4f + 4, 8);
		BlockPos pos = map.getCenterSpawnPos().offset(direction.getOpposite(), distance);

		player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), direction.asRotation(), 0);
	}

	public static void respawnIfOutOfBounds(ServerPlayerEntity player, ShardThiefMap map, ServerWorld world) {
		if (!map.getBox().contains(player.getBlockPos())) {
			ShardThiefActivePhase.spawn(world, map, player, 0);
		}
	}
}
