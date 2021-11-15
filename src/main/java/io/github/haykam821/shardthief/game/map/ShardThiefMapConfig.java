package io.github.haykam821.shardthief.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.Identifier;

public class ShardThiefMapConfig {
	public static final Codec<ShardThiefMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Identifier.CODEC.fieldOf("structure_id").forGetter(ShardThiefMapConfig::getStructureId),
			Identifier.CODEC.fieldOf("biome_id").forGetter(ShardThiefMapConfig::getBiomeId),
			RuleTest.TYPE_CODEC.optionalFieldOf("terracotta_rule", new BlockMatchRuleTest(Blocks.WHITE_TERRACOTTA)).forGetter(ShardThiefMapConfig::getTerracottaRule),
			RuleTest.TYPE_CODEC.optionalFieldOf("concrete_rule", new BlockMatchRuleTest(Blocks.WHITE_CONCRETE)).forGetter(ShardThiefMapConfig::getConcreteRule),
			RuleTest.TYPE_CODEC.optionalFieldOf("stained_glass_rule", new BlockMatchRuleTest(Blocks.WHITE_STAINED_GLASS)).forGetter(ShardThiefMapConfig::getStainedGlassRule),
			RuleTest.TYPE_CODEC.optionalFieldOf("wool_rule", new BlockMatchRuleTest(Blocks.WHITE_WOOL)).forGetter(ShardThiefMapConfig::getWoolRule),
			RuleTest.TYPE_CODEC.optionalFieldOf("carpet_rule", new BlockMatchRuleTest(Blocks.WHITE_CARPET)).forGetter(ShardThiefMapConfig::getCarpetRule),
			Codec.INT.optionalFieldOf("spawn_y_offset", 1).forGetter(ShardThiefMapConfig::getSpawnYOffset)
		).apply(instance, ShardThiefMapConfig::new);
	});

	private final Identifier structureId;
	private final Identifier biomeId;
	private final RuleTest terracottaRule;
	private final RuleTest concreteRule;
	private final RuleTest stainedGlassRule;
	private final RuleTest woolRule;
	private final RuleTest carpetRule;
	private final int spawnYOffset;

	public ShardThiefMapConfig(Identifier structureId, Identifier biomeId, RuleTest terracottaRule, RuleTest concreteRule, RuleTest stainedGlassRule, RuleTest woolRule, RuleTest carpetRule, int spawnYOffset) {
		this.structureId = structureId;
		this.biomeId = biomeId;
		this.terracottaRule = terracottaRule;
		this.concreteRule = concreteRule;
		this.stainedGlassRule = stainedGlassRule;
		this.woolRule = woolRule;
		this.carpetRule = carpetRule;
		this.spawnYOffset = spawnYOffset;
	}

	public Identifier getStructureId() {
		return this.structureId;
	}

	public Identifier getBiomeId() {
		return this.biomeId;
	}

	public RuleTest getTerracottaRule() {
		return this.terracottaRule;
	}

	public RuleTest getConcreteRule() {
		return this.concreteRule;
	}

	public RuleTest getStainedGlassRule() {
		return this.stainedGlassRule;
	}

	public RuleTest getWoolRule() {
		return this.woolRule;
	}

	public RuleTest getCarpetRule() {
		return this.carpetRule;
	}

	public int getSpawnYOffset() {
		return this.spawnYOffset;
	}
}