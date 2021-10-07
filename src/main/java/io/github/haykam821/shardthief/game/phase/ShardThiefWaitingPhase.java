package io.github.haykam821.shardthief.game.phase;

import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.map.ShardThiefGuideText;
import io.github.haykam821.shardthief.game.map.ShardThiefMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.entity.FloatingText;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;

public class ShardThiefWaitingPhase {
	private final GameSpace gameSpace;
	private final ShardThiefMap map;
	private final ShardThiefConfig config;
	private FloatingText guideText;

	public ShardThiefWaitingPhase(GameSpace gameSpace, ShardThiefMap map, ShardThiefConfig config) {
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<ShardThiefConfig> context) {
		ShardThiefConfig config = context.getConfig();
		ShardThiefMap map = new ShardThiefMap(config.getMapConfig(), context.getServer());

		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
			.setGenerator(map.createGenerator(context.getServer()))
			.setDefaultGameMode(GameMode.ADVENTURE);

		return context.createOpenProcedure(worldConfig, game -> {
			ShardThiefWaitingPhase waiting = new ShardThiefWaitingPhase(game.getGameSpace(), map, config);

			GameWaitingLobby.applyTo(game, config.getPlayerConfig());

			ShardThiefActivePhase.setRules(game, false);

			// Listeners
			game.listen(GameOpenListener.EVENT, waiting::open);
			game.listen(GameTickListener.EVENT, waiting::tick);
			game.listen(PlayerAddListener.EVENT, waiting::addPlayer);
			game.listen(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
			game.listen(RequestStartListener.EVENT, waiting::requestStart);
		});
	}

	private void open() {
		// Spawn guide text
		Vec3d center = Vec3d.of(this.map.getCenterSpawnPos()).add(0, 1.8, 0);
		this.gameSpace.getWorld().getChunk(this.map.getCenterSpawnPos());

		this.guideText = ShardThiefGuideText.spawn(this.gameSpace.getWorld(), center);
	}

	private void tick() {
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			ShardThiefActivePhase.respawnIfOutOfBounds(player, this.map, this.gameSpace.getWorld());
		}
	}

	private StartResult requestStart() {
		ShardThiefActivePhase.open(this.gameSpace, this.map, this.config, this.guideText);
		return StartResult.OK;
	}

	private void addPlayer(ServerPlayerEntity player) {
		ShardThiefActivePhase.spawn(this.gameSpace.getWorld(), this.map, player, this.gameSpace.getPlayerCount() - 1);
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player
		ShardThiefActivePhase.spawn(this.gameSpace.getWorld(), this.map, player, 0);
		return ActionResult.SUCCESS;
	}
}
