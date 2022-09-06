package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.item.DeployPlatformItem;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public class BuilderKit implements Kit {
	@Override
	public void tick(PlayerEntry player, int aliveTicks) {
		DyeColor dye = player.getTeam().getConfig().blockDyeColor();
		if (aliveTicks % 80 == 0) {
			Block wool = ColoredBlocks.wool(dye);
			if (player.hasLessThan(wool, 32)) {
				player.getPlayer().giveItemStack(new ItemStack(wool));
			}
		}
		if (aliveTicks % 200 == 0) {
			Item deployPlatform = DeployPlatformItem.ofDyeColor(dye);
			if (player.hasLessThan(deployPlatform, 5)) {
				player.getPlayer().giveItemStack(new ItemStack(deployPlatform));
			}
		}
	}
}
