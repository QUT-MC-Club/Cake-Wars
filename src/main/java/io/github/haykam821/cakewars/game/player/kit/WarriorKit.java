package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.player.PlayerEntry;

public class WarriorKit extends Kit {
	private static final int KILL_REGENERATED_HEALTH = 6;

	public WarriorKit(PlayerEntry player) {
		super(player);
	}

	@Override
	public void onKill() {
		this.player.getPlayer().heal(KILL_REGENERATED_HEALTH);
	}
}
