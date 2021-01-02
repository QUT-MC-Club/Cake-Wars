package io.github.haykam821.cakewars;

import io.github.haykam821.cakewars.game.CakeWarsConfig;
import io.github.haykam821.cakewars.game.phase.CakeWarsWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;

public class Main implements ModInitializer {
	private static final String MOD_ID = "cakewars";

	private static final Identifier CAKE_WARS_ID = new Identifier(MOD_ID, "cake_wars");
	public static final GameType<CakeWarsConfig> CAKE_WARS_TYPE = GameType.register(CAKE_WARS_ID, CakeWarsWaitingPhase::open, CakeWarsConfig.CODEC);

	@Override
	public void onInitialize() {
		return;
	}
}