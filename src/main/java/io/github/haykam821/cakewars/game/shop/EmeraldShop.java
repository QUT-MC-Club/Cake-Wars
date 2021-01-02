package io.github.haykam821.cakewars.game.shop;

import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopBuilder;
import xyz.nucleoid.plasmid.shop.ShopUi;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class EmeraldShop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.emerald");

	public static ShopUi build(PlayerEntry player) {
		return ShopUi.create(TITLE, builder -> {
			EmeraldShop.addUnbreakableItem(builder, Items.DIAMOND_HELMET, 10);
			EmeraldShop.addUnbreakableItem(builder, Items.DIAMOND_CHESTPLATE, 24);
			EmeraldShop.addUnbreakableItem(builder, Items.DIAMOND_LEGGINGS, 16);
			EmeraldShop.addUnbreakableItem(builder, Items.DIAMOND_BOOTS, 10);

			EmeraldShop.addUnbreakableItem(builder, Items.DIAMOND_SWORD, 5);
			EmeraldShop.addUnbreakableItem(builder, Items.DIAMOND_PICKAXE, 10);
			EmeraldShop.addUnbreakableItem(builder, Items.DIAMOND_AXE, 10);

			EmeraldShop.addItem(builder, Items.OBSIDIAN, 8);
			EmeraldShop.addItem(builder, Items.GOLDEN_APPLE, 8);
			EmeraldShop.addItem(builder, Items.ENDER_PEARL, 7);
		});
	}

	private static void addItem(ShopBuilder builder, ItemStack stack, int cost) {
		builder.addItem(stack, Cost.ofItem(Items.EMERALD, cost, new TranslatableText("text.cakewars.shop.emerald.cost", cost)));
	}

	private static void addItem(ShopBuilder builder, ItemConvertible item, int cost) {
		EmeraldShop.addItem(builder, new ItemStack(item), cost);
	}

	private static void addUnbreakableItem(ShopBuilder builder, ItemConvertible item, int cost) {
		EmeraldShop.addItem(builder, ItemStackBuilder.of(item).setUnbreakable().build(), cost);
	}
}
