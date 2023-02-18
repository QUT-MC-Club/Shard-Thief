package io.github.haykam821.shardthief.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.shardthief.game.map.ShardThiefMapConfig;
import net.minecraft.SharedConstants;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public class ShardThiefConfig {
	public static final Codec<ShardThiefConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			ShardThiefMapConfig.CODEC.fieldOf("map").forGetter(ShardThiefConfig::getMapConfig),
			PlayerConfig.CODEC.fieldOf("players").forGetter(ShardThiefConfig::getPlayerConfig),
			Codec.INT.optionalFieldOf("guide_ticks", 20 * 10).forGetter(ShardThiefConfig::getGuideTicks),
			Codec.INT.optionalFieldOf("starting_counts", 20).forGetter(ShardThiefConfig::getStartingCounts),
			Codec.INT.optionalFieldOf("restart_counts", 5).forGetter(ShardThiefConfig::getRestartCounts),
			Codec.INT.optionalFieldOf("count_duration", 35).forGetter(ShardThiefConfig::getCountDuration),
			Codec.INT.optionalFieldOf("shard_invulnerability", 10).forGetter(ShardThiefConfig::getShardInvulnerability),
			Codec.INT.optionalFieldOf("kit_restock_interval", 20 * 5).forGetter(ShardThiefConfig::getKitRestockInterval),
			Codec.INT.optionalFieldOf("max_arrows", 3).forGetter(ShardThiefConfig::getMaxArrows),
			Codec.INT.optionalFieldOf("speed_amplifier", 2).forGetter(ShardThiefConfig::getSpeedAmplifier),
			Codec.INT.optionalFieldOf("dropped_shard_reset_ticks", 20 * 15).forGetter(ShardThiefConfig::getDroppedShardResetTicks),
			IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("ticks_until_close", ConstantIntProvider.create(SharedConstants.TICKS_PER_SECOND * 5)).forGetter(ShardThiefConfig::getTicksUntilClose)
		).apply(instance, ShardThiefConfig::new);
	});

	private final ShardThiefMapConfig mapConfig;
	private final PlayerConfig playerConfig;
	private final int guideTicks;
	private final int startingCounts;
	private final int restartCounts;
	private final int countDuration;
	private final int shardInvulnerability;
	private final int kitRestockInterval;
	private final int maxArrows;
	private final int speedAmplifier;
	private final int droppedShardResetTicks;
	private final IntProvider ticksUntilClose;

	public ShardThiefConfig(ShardThiefMapConfig mapConfig, PlayerConfig playerConfig, int guideTicks, int startingCounts, int restartCounts, int countDuration, int shardInvulnerability, int kitRestockInterval, int maxArrows, int speedAmplifier, int droppedShardResetTicks, IntProvider ticksUntilClose) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
		this.guideTicks = guideTicks;
		this.startingCounts = startingCounts;
		this.restartCounts = restartCounts;
		this.countDuration = countDuration;
		this.shardInvulnerability = shardInvulnerability;
		this.kitRestockInterval = kitRestockInterval;
		this.maxArrows = maxArrows;
		this.speedAmplifier = speedAmplifier;
		this.droppedShardResetTicks = droppedShardResetTicks;
		this.ticksUntilClose = ticksUntilClose;
	}

	public ShardThiefMapConfig getMapConfig() {
		return this.mapConfig;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public int getGuideTicks() {
		return this.guideTicks;
	}

	public int getStartingCounts() {
		return this.startingCounts;
	}

	public int getRestartCounts() {
		return this.restartCounts;
	}

	public int getCountDuration() {
		return this.countDuration;
	}

	public int getShardInvulnerability() {
		return this.shardInvulnerability;
	}

	public int getKitRestockInterval() {
		return this.kitRestockInterval;
	}

	public int getMaxArrows() {
		return this.maxArrows;
	}

	public int getSpeedAmplifier() {
		return this.speedAmplifier;
	}

	public int getDroppedShardResetTicks() {
		return this.droppedShardResetTicks;
	}

	public IntProvider getTicksUntilClose() {
		return this.ticksUntilClose;
	}
}
