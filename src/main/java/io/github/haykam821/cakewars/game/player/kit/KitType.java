package io.github.haykam821.cakewars.game.player.kit;

import java.util.function.Function;

import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class KitType {
	private final Function<PlayerEntry, Kit> creator;
	private final Item icon;
	private final Text name;

	public KitType(Function<PlayerEntry, Kit> creator, Item icon, String key) {
		this.creator = creator;
		this.icon = icon;
		this.name = new TranslatableText("text.cakewars.kit." + key);
	}

	public Kit create(PlayerEntry player) {
		return this.creator.apply(player);
	}

	public Item getIcon() {
		return this.icon;
	}

	public Text getName() {
		return this.name;
	}
}
