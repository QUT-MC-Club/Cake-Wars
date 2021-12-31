package io.github.haykam821.cakewars;

import io.github.haykam821.cakewars.game.CakeWarsConfig;
import io.github.haykam821.cakewars.game.event.UseEntityListener;
import io.github.haykam821.cakewars.game.item.CakeWarsItems;
import io.github.haykam821.cakewars.game.phase.CakeWarsWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.EventInvokers;
import xyz.nucleoid.stimuli.Stimuli;

public class Main implements ModInitializer {
	public static final String MOD_ID = "cakewars";

	private static final Identifier CAKE_WARS_ID = new Identifier(MOD_ID, "cake_wars");
	public static final GameType<CakeWarsConfig> CAKE_WARS_TYPE = GameType.register(CAKE_WARS_ID, CakeWarsConfig.CODEC, CakeWarsWaitingPhase::open);

	public static final GameRuleType ENDER_PEARL_DAMAGE = GameRuleType.create();

	@Override
	public void onInitialize() {
		CakeWarsItems.initialize();

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient()) {
				try (EventInvokers invokers = Stimuli.select().forEntityAt(player, entity.getBlockPos())) {
					return invokers.get(UseEntityListener.EVENT).onUseEntity(player, world, hand, entity, hitResult);
				}
			}
			return ActionResult.PASS;
		});
	}
}