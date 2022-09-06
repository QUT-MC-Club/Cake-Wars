package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.player.PlayerEntry;

public class NoneKit implements Kit {
	@Override
	public void tick(PlayerEntry player, int aliveTicks) {
		return;
	}
}
