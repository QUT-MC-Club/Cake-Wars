package io.github.haykam821.cakewars.game.shop;

import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopBuilder;
import xyz.nucleoid.plasmid.shop.ShopUi;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public class BrickShop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.brick");

	public static ShopUi build(PlayerEntry player) {
		return ShopUi.create(TITLE, builder -> {
			BrickShop.addItem(builder, Items.IRON_HELMET, 5);
			BrickShop.addItem(builder, Items.IRON_CHESTPLATE, 8);
			BrickShop.addItem(builder, Items.IRON_LEGGINGS, 6);
			BrickShop.addItem(builder, Items.IRON_BOOTS, 5);

			BrickShop.addItem(builder, Items.IRON_SWORD, 5);
			BrickShop.addItem(builder, Items.IRON_PICKAXE, 8);
			BrickShop.addItem(builder, Items.IRON_AXE, 3);

			BrickShop.addItem(builder, Items.SHEARS, 10);
			BrickShop.addItem(builder, Items.ARROW, 3, 9);

			DyeColor dye = player.getTeam().getGameTeam().getDye();
			BrickShop.addItem(builder, ColoredBlocks.wool(dye), 16, 3);
			BrickShop.addItem(builder, ColoredBlocks.terracotta(dye), 8, 8);
			BrickShop.addItem(builder, Items.OAK_PLANKS, 8, 8);
			BrickShop.addItem(builder, Items.END_STONE, 8, 12);

			BrickShop.addItem(builder, Items.EMERALD, 20);
		});
	}

	private static void addItem(ShopBuilder builder, ItemStack stack, int cost) {
		builder.addItem(stack, Cost.ofItem(Items.BRICK, cost, new TranslatableText("text.cakewars.shop.brick.cost", cost)));
	}

	private static void addItem(ShopBuilder builder, ItemConvertible item, int count, int cost) {
		BrickShop.addItem(builder, new ItemStack(item, count), cost);
	}

	private static void addItem(ShopBuilder builder, ItemConvertible item, int cost) {
		BrickShop.addItem(builder, new ItemStack(item), cost);
	}
}
