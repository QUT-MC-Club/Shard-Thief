package io.github.haykam821.shardthief.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public final class ShardThiefMap {
	public static final BlockPos ORIGIN = new BlockPos(0, 64, 0);

	private final ShardThiefMapConfig mapConfig;
	private final StructureTemplate template;
	private final BlockPos centerSpawnPos;
	private final BlockBox box;

	public ShardThiefMap(ShardThiefMapConfig mapConfig, StructureTemplate template) {
		this.mapConfig = mapConfig;
		this.template = template;

		Vec3i size = this.template.getSize();
		this.centerSpawnPos = ORIGIN.add(size.getX(), this.mapConfig.getSpawnYOffset(), size.getZ());

		this.box = new BlockBox(ORIGIN.getX() + 1, ORIGIN.getY(), ORIGIN.getZ() + 1, ORIGIN.getX() + size.getX() * 2, ORIGIN.getY() + size.getY(), ORIGIN.getZ() + size.getZ() * 2);
	}

	public ShardThiefMap(ShardThiefMapConfig mapConfig, MinecraftServer server) {
		this(mapConfig, server.getStructureTemplateManager().getTemplateOrBlank(mapConfig.getStructureId()));
	}

	public ShardThiefMapConfig getMapConfig() {
		return this.mapConfig;
	}
	
	public StructureTemplate getTemplate() {
		return this.template;
	}

	public BlockPos getCenterSpawnPos() {
		return this.centerSpawnPos;
	}

	public BlockBox getBox() {
		return this.box;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new ShardThiefChunkGenerator(this.mapConfig, this.template, server);
	}
}
