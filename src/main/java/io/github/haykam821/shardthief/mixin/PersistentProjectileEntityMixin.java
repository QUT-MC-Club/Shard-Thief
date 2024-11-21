package io.github.haykam821.shardthief.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.haykam821.shardthief.game.event.AllowProjectileHitEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import xyz.nucleoid.stimuli.EventInvokers;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends ProjectileEntity {
	public PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "canHit", at = @At("HEAD"), cancellable = true)
	private void preventArrowHit(Entity entity, CallbackInfoReturnable<Boolean> ci) {
		if (entity.getWorld().isClient()) {
			return;
		}

		try (EventInvokers invokers = Stimuli.select().forEntity(entity)) {
			PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
			EventResult result = invokers.get(AllowProjectileHitEvent.EVENT).allowProjectileHit(entity, projectile);

			if (result == EventResult.DENY) {
				ci.setReturnValue(false);
			}
		}
	}
}
