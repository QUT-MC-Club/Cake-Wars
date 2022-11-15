package io.github.haykam821.cakewars.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface PlaceDeployPlatformBlockListener {
	StimulusEvent<PlaceDeployPlatformBlockListener> EVENT = StimulusEvent.create(PlaceDeployPlatformBlockListener.class, context -> {
		return (player, world, pos) -> {
			try {
				for (PlaceDeployPlatformBlockListener listener : context.getListeners()) {
					ActionResult result = listener.onPlaceDeployPlatformBlock(player, world, pos);
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

	ActionResult onPlaceDeployPlatformBlock(ServerPlayerEntity player, World world, BlockPos pos);
}