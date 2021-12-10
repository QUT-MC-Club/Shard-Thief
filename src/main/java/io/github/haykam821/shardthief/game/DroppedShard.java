package io.github.haykam821.shardthief.game;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class DroppedShard {
	private static final BlockState FULL_DROP_STATE = Blocks.PRISMARINE.getDefaultState();
	private static final BlockState SLAB_DROP_STATE = Blocks.PRISMARINE_SLAB.getDefaultState();
	private static final BlockState STAIRS_DROP_STATE = Blocks.PRISMARINE_STAIRS.getDefaultState();

	private final ShardThiefActivePhase phase;
	private final BlockPos pos;
	private final Box pickUpBox;
	private final BlockState oldState;
	private final int invulnerability;

	private int ticks = 0;

	public DroppedShard(ShardThiefActivePhase phase, BlockPos pos, BlockState oldState, int invulnerability) {
		this.phase = phase;
		this.pos = pos;
		this.pickUpBox = new Box(pos, pos.add(1, 3, 1));
		this.oldState = oldState;
		this.invulnerability = invulnerability;
	}

	public static boolean isDroppableOn(BlockState state, BlockView world, BlockPos pos) {
		Block block = state.getBlock();
		if (block instanceof SlabBlock) return true;
		if (block instanceof StairsBlock) return true;

		VoxelShape collisionShape = state.getCollisionShape(world, pos);
		return Block.isFaceFullSquare(collisionShape, Direction.UP);
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

	public void place(WorldAccess world) {
		world.setBlockState(this.pos, this.getBlockState(), 3);
	}

	public void reset(WorldAccess world) {
		world.setBlockState(this.pos, this.oldState, 3);
	}

	public Text getResetMessage() {
		return new TranslatableText("text.shardthief.dropped_shard_reset").formatted(Formatting.RED);
	}

	public boolean canPlayerPickUp(PlayerEntity player) {
		return this.ticks > this.invulnerability && this.pickUpBox.intersects(player.getBoundingBox());
	}

	public void tick() {
		this.ticks += 1;
		this.phase.attemptResetShard(this.ticks, this.pos);
	}
}