package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.item.CakeWarsItems;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;

public class FrostingKit extends Kit {
	protected static final Item SNOWBALL = CakeWarsItems.FROSTING_SNOWBALL.asItem();

	private static final int SNOWBALL_INTERVAL = SharedConstants.TICKS_PER_SECOND * 6;
	private static final int SNOWBALL_MAX_COUNT = 3;

	public FrostingKit(PlayerEntry player) {
		super(player);
	}

	@Override
	public void tick(int aliveTicks) {
		if (aliveTicks % SNOWBALL_INTERVAL == 0) {
			this.giveIfLessThan(SNOWBALL, SNOWBALL_MAX_COUNT);
		}
	}
}
