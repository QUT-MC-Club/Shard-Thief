package io.github.haykam821.shardthief.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.haykam821.shardthief.Main;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends ProjectileEntity {
	public PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "canHit", at = @At("HEAD"), cancellable = true)
	private void preventArrowBounce(Entity entity, CallbackInfoReturnable<Boolean> ci) {
		if (!(entity instanceof FallingBlockEntity)) {
			return;
		}

		ManagedGameSpace gameSpace = GameSpaceManager.get().byWorld(this.world);
		if (gameSpace != null && gameSpace.getBehavior().testRule(Main.ARROW_BOUNCE) == ActionResult.FAIL) {
			ci.setReturnValue(false);
		}
	}
}
