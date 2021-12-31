package io.github.haykam821.cakewars.game.shop;

import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGuiBuilder;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class NetherStarShop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.nether_star");

	public static GuiInterface build(PlayerEntry player) {
		SimpleGuiBuilder guiBuilder = new SimpleGuiBuilder(ScreenHandlerType.GENERIC_9X5, false);
		guiBuilder.setTitle(TITLE);

		ShopBuilderWrapper builder = new ShopBuilderWrapper(guiBuilder, player, Items.NETHER_STAR, "nether_star");

		builder.addProtectionUpgrade(Items.IRON_CHESTPLATE, 4, 10);
		builder.addSharpnessUpgrade(Items.IRON_SWORD, 8, 12);
		builder.addPowerUpgrade(Items.BOW, 8, 12);

		return guiBuilder.build(player.getPlayer());
	}
}
