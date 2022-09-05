package io.github.haykam821.cakewars.game.item;

import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class RuneOfHoldingItem extends Item implements PolymerItem {
	public RuneOfHoldingItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public Item getPolymerItem(ItemStack stack, ServerPlayerEntity player) {
		return Items.WITHER_SKELETON_SKULL;
	}
}
