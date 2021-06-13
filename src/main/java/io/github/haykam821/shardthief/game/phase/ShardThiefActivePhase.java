package io.github.haykam821.shardthief.game.phase;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import io.github.haykam821.shardthief.Main;
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
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.entity.FloatingText;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameCloseListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

public class ShardThiefActivePhase {
	private final ServerWorld world;
	private final GameSpace gameSpace;
	private final ShardThiefMap map;
	private final ShardThiefConfig config;
	private final Set<PlayerShardEntry> players;
	private final ShardThiefCountBar countBar;

	private final FloatingText guideText;
	private int guideTicks;

	private PlayerShardEntry shardHolder;
	private int ticksUntilCount;
	private int ticksUntilKitRestock;
	private DroppedShard droppedShard;

	public ShardThiefActivePhase(GameSpace gameSpace, ShardThiefMap map, ShardThiefConfig config, Set<ServerPlayerEntity> players, GlobalWidgets widgets, FloatingText guideText) {
		this.world = gameSpace.getWorld();
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;

		this.players = players.stream().map(player -> {
			return new PlayerShardEntry(player, this.config.getStartingCounts(), this.config.getShardInvulnerability());
		}).collect(Collectors.toSet());

		this.countBar = new ShardThiefCountBar(gameSpace.getGameConfig().getNameText(), widgets);

		this.placeShard(this.map.getCenterSpawnPos().down());

		this.guideText = guideText;
		this.guideTicks = this.config.getGuideTicks();
	}

	public static void setRules(GameLogic game, RuleResult pvpRule) {
		game.setRule(GameRule.CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		game.setRule(GameRule.HUNGER, RuleResult.DENY);
		game.setRule(Main.ICE_MELTING, RuleResult.DENY);
		game.setRule(GameRule.INTERACTION, RuleResult.DENY);
		game.setRule(GameRule.PORTALS, RuleResult.DENY);
		game.setRule(GameRule.PVP, pvpRule);
		game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
	}

	public static void open(GameSpace gameSpace, ShardThiefMap map, ShardThiefConfig config, FloatingText guideText) {
		gameSpace.openGame(game -> {
			GlobalWidgets widgets = new GlobalWidgets(game);

			ShardThiefActivePhase active = new ShardThiefActivePhase(gameSpace, map, config, Sets.newHashSet(gameSpace.getPlayers()), widgets, guideText);
			ShardThiefActivePhase.setRules(game, RuleResult.ALLOW);

			// Listeners
			game.on(GameCloseListener.EVENT, active::close);
			game.on(GameOpenListener.EVENT, active::open);
			game.on(GameTickListener.EVENT, active::tick);
			game.on(PlayerAddListener.EVENT, active::addPlayer);
			game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
			game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
			game.on(PlayerRemoveListener.EVENT, active::removePlayer);
		});
	}

	private void open() {
		int index = 0;
		for (PlayerShardEntry entry : this.players) {
			ServerPlayerEntity player = entry.getPlayer();

			player.setGameMode(GameMode.ADVENTURE);
			ShardInventoryManager.giveNonShardInventory(player);

			ShardThiefActivePhase.spawn(this.world, this.map, player, index);
			index += 1;
		}
	}

	private void close() {
		this.countBar.remove();
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

		this.shardHolder.getPlayer().inventory.clear();
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

		entry.getPlayer().inventory.clear();
		ShardInventoryManager.giveShardInventory(entry.getPlayer());
	}

	private void sendStealMessage() {
		Text stealText = this.shardHolder.getStealMessage();
		this.gameSpace.getPlayers().sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, stealText));
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

	private void placeShard(BlockPos pos) {
		this.droppedShard = new DroppedShard(pos, this.world.getBlockState(pos), this.config.getShardInvulnerability());
		this.droppedShard.place(this.world);
	}

	private void dropShard() {
		BlockPos pos = this.findDropPos(this.shardHolder.getPlayer().getBlockPos());
		this.placeShard(pos);

		this.clearShard();

		this.world.playSound(null, pos, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.PLAYERS, 1, 1);
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

			this.gameSpace.getPlayers().sendSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1, 1);

			this.gameSpace.close(GameCloseReason.FINISHED);
			return;
		} else if (this.shardHolder.getCounts() <= 5) {
			String countString = Integer.toString(this.shardHolder.getCounts());
			Text countText = new LiteralText(countString).formatted(this.getCountTitleColor()).formatted(Formatting.BOLD);
			this.gameSpace.getPlayers().sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, countText));

			this.gameSpace.getPlayers().sendSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.PLAYERS, 1, 1.5f);
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
			this.guideText.remove();
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
			ShardThiefActivePhase.respawnIfOutOfBounds(player, this.map, this.gameSpace.getWorld());

			if (!entry.equals(this.shardHolder) && this.canPlayerPickUpDroppedShard(player)) {
				this.pickUpShard(entry);
			}
		}
	}

	private void setSpectator(PlayerEntity player) {
		player.setGameMode(GameMode.SPECTATOR);
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.setSpectator(player);
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
