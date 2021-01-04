package io.github.haykam821.cakewars.game.shop;

import io.github.haykam821.cakewars.game.item.DeployPlatformItem;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.shop.ShopUi;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public class BrickShop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.brick");

	public static ShopUi build(PlayerEntry player) {
		return ShopUi.create(TITLE, baseBuilder -> {
			ShopBuilderWrapper builder = new ShopBuilderWrapper(baseBuilder, player, Items.BRICK, "brick");

			builder.addUnbreakableItem(Items.IRON_HELMET, 5);
			builder.addUnbreakableItem(Items.IRON_CHESTPLATE, 8);
			builder.addUnbreakableItem(Items.IRON_LEGGINGS, 6);
			builder.addUnbreakableItem(Items.IRON_BOOTS, 5);

			builder.addUnbreakableItem(Items.IRON_SWORD, 5);
			builder.addUnbreakableItem(Items.IRON_PICKAXE, 8);
			builder.addUnbreakableItem(Items.IRON_AXE, 3);

			builder.addUnbreakableItem(Items.SHEARS, 10);
			builder.addItem(Items.ARROW, 3, 9);

			DyeColor dye = player.getTeam().getGameTeam().getDye();
			builder.addItem(ColoredBlocks.wool(dye), 16, 3);
			builder.addItem(ColoredBlocks.terracotta(dye), 8, 8);
			builder.addItem(Items.OAK_PLANKS, 8, 8);
			builder.addItem(Items.END_STONE, 8, 12);
			builder.addItem(DeployPlatformItem.ofDyeColor(dye), 5);

			builder.addItem(Items.EMERALD, 20);
		});
	}
}
