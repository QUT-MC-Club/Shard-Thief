package io.github.haykam821.shardthief.game.shard;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import io.github.haykam821.shardthief.mixin.FallingBlockEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class EntityDroppedShard extends DroppedShard {
	private final Entity entity;

	public EntityDroppedShard(ShardThiefActivePhase phase, ServerWorld world, Vec3d pos, int invulnerability) {
		super(phase, pos, invulnerability);

		this.entity = createEntity(world, pos);
	}

	@Override
	public void place(WorldAccess world) {
		world.spawnEntity(this.entity);
	}

	@Override
	public void reset(WorldAccess world) {
		this.entity.discard();
	}

	private static Entity createEntity(ServerWorld world, Vec3d pos) {
		FallingBlockEntity entity = new FallingBlockEntity(EntityType.FALLING_BLOCK, world);
		entity.setPosition(pos.add(0, 0.0005, 0));
		entity.setNoGravity(true);

		FallingBlockEntityAccessor accessor = (FallingBlockEntityAccessor) entity;
		accessor.setBlockState(FULL_DROP_STATE);
		accessor.setTimeFalling(Integer.MIN_VALUE);

		return entity;
	}
}

