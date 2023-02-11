package io.github.haykam821.shardthief.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {
	@Accessor("block")
	public void setBlockState(BlockState state);

	@Accessor("timeFalling")
	public void setTimeFalling(int timeFalling);
}
