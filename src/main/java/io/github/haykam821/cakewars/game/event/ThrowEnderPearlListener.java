package io.github.haykam821.cakewars.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.event.EventType;

public interface ThrowEnderPearlListener {
	EventType<ThrowEnderPearlListener> EVENT = EventType.create(ThrowEnderPearlListener.class, listeners -> {
		return (world, user, hand) -> {
			for (ThrowEnderPearlListener listener : listeners) {
				int result = listener.onThrowEnderPearl(world, user, hand);
				if (result >= 0) {
					return result;
				}
			}
			return -1;
		};
	});

	/**
	 * @return the ender pearl cooldown to apply; if negative, delegates the cooldown to another listener or uses the default
	 */
	int onThrowEnderPearl(World world, ServerPlayerEntity user, Hand hand);
}