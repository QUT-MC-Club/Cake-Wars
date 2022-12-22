package io.github.haykam821.cakewars.game.shop;

import eu.pb4.sgui.api.gui.BaseSlotGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

public abstract class Shop {
	protected abstract Text getTitle();

	protected abstract Item getCurrency();

	protected abstract String getCurrencyName();

	protected abstract void addItems(PlayerEntry player, ShopBuilderWrapper builder);

	public BaseSlotGui build(PlayerEntry player) {
		BaseSlotGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player.getPlayer(), false);
		gui.setTitle(this.getTitle());

		this.update(player, gui, false);

		return gui;
	}

	public void update(PlayerEntry player, BaseSlotGui gui, boolean clear) {
		if (clear) {
			for (int slot = 0; slot < gui.getSize(); slot++) {
				gui.clearSlot(slot);
			}
		}

		ShopBuilderWrapper builder = new ShopBuilderWrapper(gui, player, this.getCurrency(), this.getCurrencyName());
		this.addItems(player, builder);
	}
}
