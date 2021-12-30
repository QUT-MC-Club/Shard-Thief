package io.github.haykam821.shardthief.game.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructuresConfig;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;

public final class ShardThiefChunkGenerator extends GameChunkGenerator {
	private final ShardThiefMapConfig mapConfig;
	private final Structure structure;

	public ShardThiefChunkGenerator(ShardThiefMapConfig mapConfig, Structure structure, MinecraftServer server) {
		super(GameChunkGenerator.createBiomeSource(server, RegistryKey.of(Registry.BIOME_KEY, mapConfig.getBiomeId())), new StructuresConfig(Optional.empty(), Collections.emptyMap()));

		this.mapConfig = mapConfig;
		this.structure = structure;
	}

	private StructureProcessorRule getReplaceRule(RuleTest match, Block output) {
		return new StructureProcessorRule(
			match,
			AlwaysTrueRuleTest.INSTANCE,
			output.getDefaultState()
		);
	}

	private List<StructureProcessorRule> getRules(Block terracotta, Block concrete, Block stainedGlass, Block wool, Block carpet) {
		List<StructureProcessorRule> rules = new ArrayList<>();

		rules.add(this.getReplaceRule(this.mapConfig.getTerracottaRule(), terracotta));
		rules.add(this.getReplaceRule(this.mapConfig.getConcreteRule(), concrete));
		rules.add(this.getReplaceRule(this.mapConfig.getStainedGlassRule(), stainedGlass));
		rules.add(this.getReplaceRule(this.mapConfig.getWoolRule(), wool));
		rules.add(this.getReplaceRule(this.mapConfig.getCarpetRule(), carpet));
	
		return rules;
	}

	private void placeStructure(StructureWorldAccess world, Chunk chunk, BlockPos pos, BlockRotation rotation, Block terracotta, Block concrete, Block stainedGlass, Block wool, Block carpet) {
		StructurePlacementData placementData = new StructurePlacementData();

		placementData.setRotation(rotation);
		placementData.addProcessor(new RuleStructureProcessor(this.getRules(terracotta, concrete, stainedGlass, wool, carpet)));


		ChunkPos chunkPos = chunk.getPos();
		BlockBox chunkBox = new BlockBox(chunkPos.getStartX(), chunk.getBottomY(), chunkPos.getStartZ(), chunkPos.getEndX(), chunk.getTopY(), chunkPos.getEndZ());

		if (!chunkBox.intersects(this.structure.calculateBoundingBox(placementData, pos))) return;
		placementData.setBoundingBox(chunkBox);

		this.structure.place(world, pos, pos, placementData, world.getRandom(), Block.NO_REDRAW);
	}

	@Override
	public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structures) {
		Vec3i size = this.structure.getSize();
		int x = size.getX() * 2 - 1;
		int z = size.getZ() * 2 - 1;

		this.placeStructure(world, chunk, ShardThiefMap.ORIGIN, BlockRotation.NONE, Blocks.LIME_TERRACOTTA, Blocks.LIME_CONCRETE, Blocks.LIME_STAINED_GLASS, Blocks.LIME_WOOL, Blocks.LIME_CARPET);
		this.placeStructure(world, chunk, ShardThiefMap.ORIGIN.add(x, 0, 0), BlockRotation.CLOCKWISE_90, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.BLUE_CONCRETE, Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_WOOL, Blocks.BLUE_CARPET);
		this.placeStructure(world, chunk, ShardThiefMap.ORIGIN.add(x, 0, z), BlockRotation.CLOCKWISE_180, Blocks.RED_TERRACOTTA, Blocks.RED_CONCRETE, Blocks.RED_STAINED_GLASS, Blocks.RED_WOOL, Blocks.RED_CARPET);
		this.placeStructure(world, chunk, ShardThiefMap.ORIGIN.add(0, 0, z), BlockRotation.COUNTERCLOCKWISE_90, Blocks.YELLOW_TERRACOTTA, Blocks.YELLOW_CONCRETE, Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_WOOL, Blocks.YELLOW_CARPET);
	}
}
