package io.github.haykam821.cakewars.game.shop;

import io.github.haykam821.cakewars.game.item.DeployPlatformItem;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public class BrickShop extends Shop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.brick");

	@Override
	protected Text getTitle() {
		return TITLE;
	}

	@Override
	protected Item getCurrency() {
		return Items.BRICK;
	}

	@Override
	protected String getCurrencyName() {
		return "brick";
	}

	@Override
	protected void addItems(PlayerEntry player, ShopBuilderWrapper builder) {
		builder.addArmorItem((ArmorItem) Items.IRON_HELMET, 5);
		builder.addArmorItem((ArmorItem) Items.IRON_CHESTPLATE, 8);
		builder.addArmorItem((ArmorItem) Items.IRON_LEGGINGS, 6);
		builder.addArmorItem((ArmorItem) Items.IRON_BOOTS, 5);

		builder.addUnbreakableItem(Items.IRON_SWORD, 5);
		builder.addUnbreakableItem(Items.BOW, 12);
		builder.addUnbreakableItem(Items.IRON_PICKAXE, 8);
		builder.addUnbreakableItem(Items.IRON_AXE, 3);

		builder.addItem(Items.ARROW, 3, 9);

		DyeColor dye = player.getTeam().getConfig().blockDyeColor();
		builder.addItem(ColoredBlocks.wool(dye), 16, 3);
		builder.addItem(ColoredBlocks.terracotta(dye), 8, 8);
		builder.addItem(Items.OAK_PLANKS, 8, 8);
		builder.addItem(Items.LADDER, 8, 8);
		builder.addItem(Items.END_STONE, 8, 12);
		builder.addItem(DeployPlatformItem.ofDyeColor(dye), 5);

		builder.addItem(Items.EMERALD, 20);
	}
}
