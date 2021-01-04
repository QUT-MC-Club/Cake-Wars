package io.github.haykam821.cakewars.game.shop;

import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.shop.ShopUi;

public class EmeraldShop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.emerald");

	public static ShopUi build(PlayerEntry player) {
		return ShopUi.create(TITLE, baseBuilder -> {
			ShopBuilderWrapper builder = new ShopBuilderWrapper(baseBuilder, player, Items.EMERALD, "emerald");

			builder.addUnbreakableItem(Items.DIAMOND_HELMET, 10);
			builder.addUnbreakableItem(Items.DIAMOND_CHESTPLATE, 24);
			builder.addUnbreakableItem(Items.DIAMOND_LEGGINGS, 16);
			builder.addUnbreakableItem(Items.DIAMOND_BOOTS, 10);

			builder.addUnbreakableItem(Items.DIAMOND_SWORD, 5);
			builder.addUnbreakableItem(Items.DIAMOND_PICKAXE, 10);
			builder.addUnbreakableItem(Items.DIAMOND_AXE, 10);

			builder.addItem(Items.OBSIDIAN, 8);
			builder.addItem(Items.GOLDEN_APPLE, 8);
			builder.addItem(Items.ENDER_PEARL, 7);
		});
	}
}
