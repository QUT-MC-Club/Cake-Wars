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
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;

public class Main implements ModInitializer {
	public static final String MOD_ID = "cakewars";

	private static final Identifier CAKE_WARS_ID = new Identifier(MOD_ID, "cake_wars");
	public static final GameType<CakeWarsConfig> CAKE_WARS_TYPE = GameType.register(CAKE_WARS_ID, CakeWarsWaitingPhase::open, CakeWarsConfig.CODEC);

	public static final GameRule ENDER_PEARL_DAMAGE = new GameRule();

	@Override
	public void onInitialize() {
		CakeWarsItems.initialize();

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient()) return ActionResult.PASS;

			ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
			if (gameSpace == null) return ActionResult.PASS;

			if (!gameSpace.containsEntity(player)) return ActionResult.PASS;
			if (!gameSpace.containsEntity(entity)) return ActionResult.PASS;

			return gameSpace.invoker(UseEntityListener.EVENT).onUseEntity(player, world, hand, entity, hitResult);
		});
	}
}