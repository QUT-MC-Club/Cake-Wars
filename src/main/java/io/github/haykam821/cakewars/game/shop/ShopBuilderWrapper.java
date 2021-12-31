package io.github.haykam821.cakewars.game.shop;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import eu.pb4.sgui.api.gui.SimpleGuiBuilder;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import io.github.haykam821.cakewars.game.player.team.TeamEntry;
import io.github.haykam821.cakewars.game.player.team.TeamUpgrades;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class ShopBuilderWrapper {
	private final SimpleGuiBuilder baseBuilder;
	private final PlayerEntry entry;
	private final Item currency;
	private final String label;

	protected ShopBuilderWrapper(SimpleGuiBuilder baseBuilder, PlayerEntry entry, Item currency, String currencyName) {
		this.baseBuilder = baseBuilder;
		this.entry = entry;

		this.currency = currency;
		this.label = "text.cakewars.shop." + currencyName + ".cost";
	}

	private Cost createCost(int cost) {
		return Cost.ofItem(this.currency, cost, new TranslatableText(this.label, cost));
	}

	protected void addItem(ItemStack stack, int cost) {
		this.baseBuilder.addSlot(ShopEntry.buyItem(stack).withCost(this.createCost(cost)).onBuy(player -> {
			player.getInventory().offerOrDrop(stack);
			this.entry.applyUpgrades();
		}));
	}

	protected void addItem(ItemConvertible item, int cost) {
		this.addItem(new ItemStack(item), cost);
	}

	protected void addItem(ItemConvertible item, int count, int cost) {
		this.addItem(new ItemStack(item, count), cost);
	}

	protected void addUnbreakableItem(ItemConvertible item, int cost) {
		this.addItem(ItemStackBuilder.of(item).setUnbreakable().build(), cost);
	}

	protected void addArmorItem(ItemConvertible item, EquipmentSlot slot, int costInt) {
		ItemStack iconStack = new ItemStack(item);
		Cost cost = this.createCost(costInt);

		this.baseBuilder.addSlot(ShopEntry.ofIcon(iconStack).withCost(cost).onBuy(player -> {
			player.equipStack(slot, ItemStackBuilder.of(item).setUnbreakable().build());
			this.entry.applyUpgrades();
		}));
	}

	protected void addArmorItem(ArmorItem item, int cost) {
		this.addArmorItem(item, item.getSlotType(), cost);
	}

	private void addTeamUpgrade(String key, IntSupplier getter, IntConsumer setter, ItemConvertible icon, int... costs) {
		int level = getter.getAsInt();

		Text levelText = new TranslatableText("enchantment.level." + (level + 1));
		Text name = new TranslatableText("text.cakewars.upgrade." + key, levelText);
		ItemStack iconStack = new ItemStack(icon).setCustomName(name);

		Cost cost;
		if (level >= costs.length) {
			cost = Cost.no();
		} else {
			cost = this.createCost(costs[level]);
		}

		this.baseBuilder.addSlot(ShopEntry.ofIcon(iconStack).withCost(cost).onBuy(player -> {
			setter.accept(level + 1);
			for (PlayerEntry entry : this.entry.getPhase().getPlayers()) {
				if (this.entry.getTeam() == entry.getTeam()) {
					entry.applyUpgrades();
				}
			}

			// Message
			TeamEntry team = this.entry.getTeam();
			Text message = new TranslatableText("text.cakewars.upgrade_bought", player.getDisplayName(), name, team.getName()).formatted(Formatting.GOLD);
			team.sendMessageIncludingSpectators(message);
		}));
	}

	protected void addProtectionUpgrade(ItemConvertible icon, int... costs) {
		TeamUpgrades upgrades = this.entry.getTeam().getUpgrades();
		this.addTeamUpgrade("protection", upgrades::getProtection, upgrades::setProtection, icon, costs);
	}

	protected void addSharpnessUpgrade(ItemConvertible icon, int... costs) {
		TeamUpgrades upgrades = this.entry.getTeam().getUpgrades();
		this.addTeamUpgrade("sharpness", upgrades::getSharpness, upgrades::setSharpness, icon, costs);
	}

	protected void addPowerUpgrade(ItemConvertible icon, int... costs) {
		TeamUpgrades upgrades = this.entry.getTeam().getUpgrades();
		this.addTeamUpgrade("power", upgrades::getPower, upgrades::setPower, icon, costs);
	}
}
