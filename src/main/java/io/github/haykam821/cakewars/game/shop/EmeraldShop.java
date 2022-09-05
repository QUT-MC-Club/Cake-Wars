package io.github.haykam821.cakewars.game.shop;

import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGuiBuilder;
import io.github.haykam821.cakewars.game.item.CakeWarsItems;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class EmeraldShop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.emerald");

	public static GuiInterface build(PlayerEntry player) {
		SimpleGuiBuilder guiBuilder = new SimpleGuiBuilder(ScreenHandlerType.GENERIC_9X5, false);
		guiBuilder.setTitle(TITLE);

		ShopBuilderWrapper builder = new ShopBuilderWrapper(guiBuilder, player, Items.EMERALD, "emerald");

		builder.addArmorItem((ArmorItem) Items.DIAMOND_HELMET, 10);
		builder.addArmorItem((ArmorItem) Items.DIAMOND_CHESTPLATE, 24);
		builder.addArmorItem((ArmorItem) Items.DIAMOND_LEGGINGS, 16);
		builder.addArmorItem((ArmorItem) Items.DIAMOND_BOOTS, 10);

		builder.addUnbreakableItem(Items.DIAMOND_SWORD, 5);
		builder.addUnbreakableItem(Items.DIAMOND_PICKAXE, 10);
		builder.addUnbreakableItem(Items.DIAMOND_AXE, 4);

		builder.addItem(Items.OBSIDIAN, 12);
		builder.addUnbreakableItem(Items.SHEARS, 5);
		builder.addItem(Items.GOLDEN_APPLE, 8);
		builder.addItem(Items.ENDER_PEARL, 7);
		builder.addItem(CakeWarsItems.RUNE_OF_HOLDING, 20);

		return guiBuilder.build(player.getPlayer());
	}
}
