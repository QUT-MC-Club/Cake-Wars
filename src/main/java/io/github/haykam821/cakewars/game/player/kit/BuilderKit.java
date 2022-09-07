package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.item.DeployPlatformItem;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public class BuilderKit extends Kit {
	public BuilderKit(PlayerEntry player) {
		super(player);
	}

	@Override
	public void tick(int aliveTicks) {
		DyeColor dye = this.player.getTeam().getConfig().blockDyeColor();
		if (aliveTicks % 80 == 0) {
			Block wool = ColoredBlocks.wool(dye);
			this.giveIfLessThan(wool, 32);
		}
		if (aliveTicks % 200 == 0) {
			Item deployPlatform = DeployPlatformItem.ofDyeColor(dye);
			this.giveIfLessThan(deployPlatform, 5);
		}
	}
}
