package io.github.haykam821.shardthief;

import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.phase.ShardThiefWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.GameType;

public class Main implements ModInitializer {
	private static final String MOD_ID = "shardthief";

	private static final Identifier SHARD_THIEF_ID = Main.identifier("shard_thief");
	public static final GameType<ShardThiefConfig> SHARD_THIEF_TYPE = GameType.register(SHARD_THIEF_ID, ShardThiefConfig.CODEC, ShardThiefWaitingPhase::open);

	@Override
	public void onInitialize() {
		return;
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
