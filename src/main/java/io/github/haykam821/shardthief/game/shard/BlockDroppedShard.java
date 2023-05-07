package io.github.haykam821.shardthief.game.shard;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BlockDroppedShard extends DroppedShard {
	private final BlockPos blockPos;
	private final BlockState oldState;

	public BlockDroppedShard(ShardThiefActivePhase phase, BlockPos pos, BlockState oldState, int invulnerability) {
		super(phase, Vec3d.ofCenter(pos), invulnerability);

		this.blockPos = pos;
		this.oldState = oldState;
	}

	private BlockState getBlockState() {
		Block block = this.oldState.getBlock();
		if (block instanceof SlabBlock) {
			return SLAB_DROP_STATE
				.with(Properties.SLAB_TYPE, this.oldState.get(Properties.SLAB_TYPE))
				.with(Properties.WATERLOGGED, this.oldState.get(Properties.WATERLOGGED));
		} else if (block instanceof StairsBlock) {
			return STAIRS_DROP_STATE
				.with(HorizontalFacingBlock.FACING, this.oldState.get(HorizontalFacingBlock.FACING))
				.with(Properties.BLOCK_HALF, this.oldState.get(Properties.BLOCK_HALF))
				.with(Properties.STAIR_SHAPE, this.oldState.get(Properties.STAIR_SHAPE))
				.with(Properties.WATERLOGGED, this.oldState.get(Properties.WATERLOGGED));
		}

		return FULL_DROP_STATE;
	}

	@Override
	public void place(ServerWorld world) {
		world.setBlockState(this.blockPos, this.getBlockState(), 3);
	}

	@Override
	public void reset(ServerWorld world) {
		world.setBlockState(this.blockPos, this.oldState, 3);
	}
}
