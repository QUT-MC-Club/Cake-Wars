package io.github.haykam821.cakewars.game.item;

import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.SharedConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.server.network.ServerPlayerEntity;

public class FrostingSnowballItem extends SnowballItem implements PolymerItem {
	public static final int SLOWNESS_DURATION = (int) (SharedConstants.TICKS_PER_SECOND * 0.5);

	public FrostingSnowballItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public Item getPolymerItem(ItemStack stack, ServerPlayerEntity player) {
		return Items.SNOWBALL;
	}

	public static void applySlowness(Entity entity) {
		if (entity instanceof LivingEntity) {
			StatusEffectInstance effect = new StatusEffectInstance(StatusEffects.SLOWNESS, SLOWNESS_DURATION, 0, false, true, false);
			((LivingEntity) entity).addStatusEffect(effect);
		}
	}
}
