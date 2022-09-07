package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.item.CakeWarsItems;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FrostingKit implements Kit {
	protected static final Item SNOWBALL = CakeWarsItems.FROSTING_SNOWBALL.asItem();

	private static final int SNOWBALL_INTERVAL = SharedConstants.TICKS_PER_SECOND * 6;
	private static final int SNOWBALL_MAX_COUNT = 3;

	@Override
	public void tick(PlayerEntry player, int aliveTicks) {
		if (aliveTicks % SNOWBALL_INTERVAL == 0) {
			if (player.hasLessThan(SNOWBALL, SNOWBALL_MAX_COUNT)) {
				player.getPlayer().giveItemStack(new ItemStack(SNOWBALL));
			}
		}
	}
}
