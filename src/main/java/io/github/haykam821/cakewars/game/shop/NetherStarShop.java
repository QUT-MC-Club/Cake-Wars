package io.github.haykam821.cakewars.game.shop;

import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.shop.ShopUi;

public class NetherStarShop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.nether_star");

	public static ShopUi build(PlayerEntry player) {
		return ShopUi.create(TITLE, baseBuilder -> {
			ShopBuilderWrapper builder = new ShopBuilderWrapper(baseBuilder, player, Items.NETHER_STAR, "nether_star");

			builder.addProtectionUpgrade(Items.IRON_CHESTPLATE, 4, 10);
			builder.addSharpnessUpgrade(Items.IRON_SWORD, 8, 12);
			builder.addPowerUpgrade(Items.BOW, 8, 12);
		});
	}
}
