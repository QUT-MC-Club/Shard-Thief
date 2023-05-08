package io.github.haykam821.shardthief.game.shard;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

public class DisplayDroppedShard extends DroppedShard {
	private final ElementHolder holder;
	private HolderAttachment attachment;

	public DisplayDroppedShard(ShardThiefActivePhase phase, ServerWorld world, Vec3d pos, int invulnerability) {
		super(phase, pos, invulnerability);

		this.holder = createHolder(world, pos);
	}

	@Override
	public void place(ServerWorld world) {
		this.attachment = ChunkAttachment.of(this.holder, world, this.getPos().add(-0.5, 0.0005, -0.5));
	}

	@Override
	public void reset(ServerWorld world) {
		this.attachment.destroy();
		this.attachment = null;
	}

	private static ElementHolder createHolder(ServerWorld world, Vec3d pos) {
		BlockPos lightPos = BlockPos.ofFloored(pos.getX(), pos.getY() + 1, pos.getZ());

		int blockLight = world.getLightLevel(LightType.BLOCK, lightPos);
		int skyLight = world.getLightLevel(LightType.SKY, lightPos);

		BlockDisplayElement element = new BlockDisplayElement(FULL_DROP_STATE);
		element.setBrightness(new Brightness(blockLight, skyLight));

		ElementHolder holder = new ElementHolder();
		holder.addElement(element);

		return holder;
	}
}

