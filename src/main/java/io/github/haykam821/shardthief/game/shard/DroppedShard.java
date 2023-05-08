package io.github.haykam821.shardthief.game.shard;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public abstract class DroppedShard {
	protected static final BlockState FULL_DROP_STATE = Blocks.PRISMARINE.getDefaultState();
	protected static final BlockState SLAB_DROP_STATE = Blocks.PRISMARINE_SLAB.getDefaultState();
	protected static final BlockState STAIRS_DROP_STATE = Blocks.PRISMARINE_STAIRS.getDefaultState();

	private static final int TICKS_PER_PARTICLE = 2;
	private static final double PARTICLE_Y_OFFSET = 1.1;
	private static final double PARTICLE_MIN_DIAMETER = 0.2;
	private static final double PARTICLE_DIAMETER_VARIANCE = 0.05;
	private static final double PARTICLE_SPEED = 0.12;

	private final ShardThiefActivePhase phase;
	private final Vec3d pos;
	private final Box pickUpBox;
	private final int invulnerability;

	private int ticks = 0;

	public DroppedShard(ShardThiefActivePhase phase, Vec3d pos, int invulnerability) {
		this.phase = phase;
		this.pos = pos;
		this.pickUpBox = new Box(pos.subtract(0.5, 0, 0.5), pos.add(0.5, 3, 0.5));
		this.invulnerability = invulnerability;
	}

	public static boolean isDroppableOn(BlockState state, BlockView world, BlockPos pos) {
		Block block = state.getBlock();
		if (block instanceof SlabBlock) return true;
		if (block instanceof StairsBlock) return true;

		VoxelShape collisionShape = state.getCollisionShape(world, pos);
		return Block.isFaceFullSquare(collisionShape, Direction.UP);
	}

	public abstract void place(ServerWorld world);

	public abstract void reset(ServerWorld world);

	public Text getResetMessage() {
		return Text.translatable("text.shardthief.dropped_shard_reset").formatted(Formatting.RED);
	}

	public Vec3d getPos() {
		return this.pos;
	}

	private boolean hasInvulnerability() {
		return this.ticks <= this.invulnerability;
	}

	public boolean canPlayerPickUp(PlayerEntity player) {
		return !this.hasInvulnerability() && this.pickUpBox.intersects(player.getBoundingBox());
	}

	private void spawnParticles(ServerWorld world) {
		Random random = world.getRandom();
		double angle = random.nextDouble() * Math.PI * 2;
		double diameter = random.nextDouble() * PARTICLE_DIAMETER_VARIANCE + PARTICLE_MIN_DIAMETER;
		
		double x = this.pos.getX() + (Math.sin(angle) * diameter);
		double y = this.pos.getY() + PARTICLE_Y_OFFSET;
		double z = this.pos.getZ() + (Math.cos(angle) * diameter);

		world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0, 0.8, 0, PARTICLE_SPEED);
	}

	public void tick(ServerWorld world) {
		this.ticks += 1;
		this.phase.attemptResetShard(this.ticks, this.pos);

		if (!this.hasInvulnerability() && this.ticks % TICKS_PER_PARTICLE == 0) {
			this.spawnParticles(world);
		}
	}
}