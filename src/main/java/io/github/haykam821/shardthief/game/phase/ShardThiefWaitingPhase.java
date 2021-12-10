package io.github.haykam821.shardthief.game.phase;

import eu.pb4.holograms.api.holograms.AbstractHologram;
import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.map.ShardThiefGuideText;
import io.github.haykam821.shardthief.game.map.ShardThiefMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class ShardThiefWaitingPhase {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final ShardThiefMap map;
	private final ShardThiefConfig config;
	private AbstractHologram guideText;

	public ShardThiefWaitingPhase(GameSpace gameSpace, ServerWorld world, ShardThiefMap map, ShardThiefConfig config) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<ShardThiefConfig> context) {
		ShardThiefConfig config = context.config();
		ShardThiefMap map = new ShardThiefMap(config.getMapConfig(), context.server());

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			ShardThiefWaitingPhase waiting = new ShardThiefWaitingPhase(activity.getGameSpace(), world, map, config);

			GameWaitingLobby.addTo(activity, config.getPlayerConfig());

			ShardThiefActivePhase.setRules(activity, false);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, waiting::enable);
			activity.listen(GameActivityEvents.TICK, waiting::tick);
			activity.listen(GamePlayerEvents.OFFER, waiting::offerPlayer);
			activity.listen(PlayerDamageEvent.EVENT, waiting::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
			activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
		});
	}

	private void enable() {
		// Spawn guide text
		Vec3d center = Vec3d.of(this.map.getCenterSpawnPos()).add(0, 1.8, 0);
		this.guideText = ShardThiefGuideText.spawn(this.world, center);
	}

	private void tick() {
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			ShardThiefActivePhase.respawnIfOutOfBounds(player, this.map, this.world);
		}
	}

	private GameResult requestStart() {
		ShardThiefActivePhase.open(this.gameSpace, this.world, this.map, this.config, this.guideText);
		return GameResult.ok();
	}

	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, Vec3d.ZERO).and(() -> {
			offer.player().changeGameMode(GameMode.ADVENTURE);
			ShardThiefActivePhase.spawn(this.world, this.map, offer.player(), this.gameSpace.getPlayers().size() - 1);
		});
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player
		ShardThiefActivePhase.spawn(this.world, this.map, player, 0);
		return ActionResult.FAIL;
	}
}
