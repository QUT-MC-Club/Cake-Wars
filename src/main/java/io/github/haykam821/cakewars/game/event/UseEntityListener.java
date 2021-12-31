package io.github.haykam821.cakewars.game.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface UseEntityListener {
	StimulusEvent<UseEntityListener> EVENT = StimulusEvent.create(UseEntityListener.class, context -> {
		return (player, world, hand, entity, hitResult) -> {
			try {
				for (UseEntityListener listener : context.getListeners()) {
					ActionResult result = listener.onUseEntity(player, world, hand, entity, hitResult);
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

	ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult);
}