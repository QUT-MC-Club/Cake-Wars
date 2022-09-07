package io.github.haykam821.cakewars.game.shop;

import io.github.haykam821.cakewars.game.item.CakeWarsItems;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class EmeraldShop extends Shop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.emerald");

	@Override
	protected Text getTitle() {
		return TITLE;
	}

	@Override
	protected Item getCurrency() {
		return Items.EMERALD;
	}

	@Override
	protected String getCurrencyName() {
		return "emerald";
	}

	@Override
	protected void addItems(PlayerEntry player, ShopBuilderWrapper builder) {
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

		if (player.getTeam().hasCake()) {
			builder.addItem(CakeWarsItems.RUNE_OF_HOLDING, 20);
		}
	}
}
