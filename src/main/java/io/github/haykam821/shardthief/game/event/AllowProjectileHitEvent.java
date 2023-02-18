package io.github.haykam821.shardthief.game.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

/**
 * Called when a projectile attempts to hit an entity.
 *
 * <p>Unlike {@link PlayerDamageEvent}, this event can be used to prevent
 * projectiles from bouncing off players that cannot be damaged.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and hits the entity.
 * <li>{@link ActionResult#FAIL} cancels further processing and does not hit the entity.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 *
 * <p>If all listeners return {@link ActionResult#PASS},
 * the projectile hits the player as per normal behavior.
 */
public interface AllowProjectileHitEvent {
	StimulusEvent<AllowProjectileHitEvent> EVENT = StimulusEvent.create(AllowProjectileHitEvent.class, context -> {
		return (entity, projectile) -> {
			try {
				for (AllowProjectileHitEvent listener : context.getListeners()) {
					ActionResult result = listener.allowProjectileHit(entity, projectile);
					if (result != ActionResult.PASS) {
						return result;
					}
				}
			} catch (Throwable throwable) {
				context.handleException(throwable);
			}
			return ActionResult.PASS;
		};
	});

	ActionResult allowProjectileHit(Entity entity, PersistentProjectileEntity projectile);
}