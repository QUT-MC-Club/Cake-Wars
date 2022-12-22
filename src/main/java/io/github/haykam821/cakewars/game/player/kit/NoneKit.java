package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.player.PlayerEntry;

public class NoneKit extends Kit {
	public NoneKit(PlayerEntry player) {
		super(player);
	}

	@Override
	public void tick(int aliveTicks) {
		return;
	}
}
