package io.github.haykam821.cakewars.game.player.team;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class TeamUpgrades {
	private int protection = 0;
	private int sharpness = 0;
	private int power = 0;

	/**
	 * Applies enchantment-based upgrades to a given stack.
	 */
	public ItemStack applyTo(ItemStack stack) {
		if (this.protection > 0 && stack.getItem() instanceof ArmorItem) {
			stack.removeSubNbt("Enchantments");
			stack.addEnchantment(Enchantments.PROTECTION, this.protection);
		}
		if (this.sharpness > 0 && stack.getItem() instanceof SwordItem) {
			stack.removeSubNbt("Enchantments");
			stack.addEnchantment(Enchantments.SHARPNESS, this.sharpness);
		}
		if (this.power > 0 && stack.getItem() instanceof BowItem) {
			stack.removeSubNbt("Enchantments");
			stack.addEnchantment(Enchantments.POWER, this.power);
		}

		return stack;
	}

	public int getProtection() {
		return this.protection;
	}

	public void setProtection(int protection) {
		this.protection = protection;
	}

	public int getSharpness() {
		return this.sharpness;
	}

	public void setSharpness(int sharpness) {
		this.sharpness = sharpness;
	}

	public int getPower() {
		return this.power;
	}

	public void setPower(int power) {
		this.power = power;
	}
}
