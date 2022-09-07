package io.github.haykam821.cakewars.game.shop;

import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class NetherStarShop extends Shop {
	private static final Text TITLE = new TranslatableText("text.cakewars.shop.nether_star");

	@Override
	protected Text getTitle() {
		return TITLE;
	}

	@Override
	protected Item getCurrency() {
		return Items.NETHER_STAR;
	}

	@Override
	protected String getCurrencyName() {
		return "nether_star";
	}

	@Override
	protected void addItems(PlayerEntry player, ShopBuilderWrapper builder) {
		builder.addProtectionUpgrade(Items.IRON_CHESTPLATE, 4, 10);
		builder.addSharpnessUpgrade(Items.IRON_SWORD, 8, 12);
		builder.addPowerUpgrade(Items.BOW, 8, 12);
	}
}
