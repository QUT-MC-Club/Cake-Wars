package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.player.PlayerEntry;

public interface Kit {
	public void tick(PlayerEntry player, int aliveTicks);
}
