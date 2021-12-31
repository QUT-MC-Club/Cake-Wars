package io.github.haykam821.cakewars.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface ThrowEnderPearlListener {
	StimulusEvent<ThrowEnderPearlListener> EVENT = StimulusEvent.create(ThrowEnderPearlListener.class, context -> {
		return (world, user, hand) -> {
			try {
				for (ThrowEnderPearlListener listener : context.getListeners()) {
					int result = listener.onThrowEnderPearl(world, user, hand);
					if (result >= 0) {
						return result;
					}
				}
			} catch (Throwable throwable) {
				context.handleException(throwable);
			}
			return -1;
		};
	});

	/**
	 * @return the ender pearl cooldown to apply; if negative, delegates the cooldown to another listener or uses the default
	 */
	int onThrowEnderPearl(World world, ServerPlayerEntity user, Hand hand);
}