package io.github.haykam821.cakewars.game.player.kit.selection;

import java.util.HashMap;
import java.util.Map;

import io.github.haykam821.cakewars.game.player.kit.KitType;
import io.github.haykam821.cakewars.game.player.kit.KitTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

public class KitSelectionManager {
	private final Map<ServerPlayerEntity, KitType> selections = new HashMap<>();

	public KitType get(ServerPlayerEntity player, Random random) {
		KitType selection = this.selections.get(player);
		if (selection != null) return selection;

		return Util.getRandom(KitTypes.KITS, random);
	}

	public void select(ServerPlayerEntity player, KitType selection) {
		this.selections.put(player, selection);
	}

	public void deselect(ServerPlayerEntity player) {
		this.selections.remove(player);
	}
}
