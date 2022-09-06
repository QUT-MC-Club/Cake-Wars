package io.github.haykam821.cakewars.game.player.kit;

import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class KitType {
	private final Supplier<Kit> creator;
	private final Item icon;
	private final Text name;

	public KitType(Supplier<Kit> creator, Item icon, String key) {
		this.creator = creator;
		this.icon = icon;
		this.name = new TranslatableText("text.cakewars.kit." + key);
	}

	public Kit create() {
		return this.creator.get();
	}

	public Item getIcon() {
		return this.icon;
	}

	public Text getName() {
		return this.name;
	}
}
