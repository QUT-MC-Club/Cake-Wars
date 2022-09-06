package io.github.haykam821.cakewars.game.player.kit.selection;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SimpleGuiBuilder;
import io.github.haykam821.cakewars.game.player.kit.KitType;
import io.github.haykam821.cakewars.game.player.kit.KitTypes;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class KitSelectionUi {
	private static final Formatting RANDOM_FORMATTING = Formatting.LIGHT_PURPLE;
	private static final Formatting KIT_FORMATTING = Formatting.GREEN;

	private static final Text TITLE = new TranslatableText("text.cakewars.kit_selection.title");
	private static final Text RANDOM_KIT = new TranslatableText("text.cakewars.kit_selection.random_kit").formatted(RANDOM_FORMATTING);

	private static void addKit(SimpleGuiBuilder builder, KitSelectionManager kitSelection, ServerPlayerEntity player, KitType kitType) {
		Text name = kitType.getName().shallowCopy().formatted(KIT_FORMATTING);

		builder.addSlot(new GuiElementBuilder(kitType.getIcon())
			.setName(name)
			.setCallback((index, type, action) -> {
				kitSelection.select(player, kitType);
				KitSelectionUi.playClickSound(player);
			}));

	}

	public static SimpleGui build(KitSelectionManager kitSelection, ServerPlayerEntity player) {
		SimpleGuiBuilder builder = new SimpleGuiBuilder(ScreenHandlerType.GENERIC_9X1, false);

		builder.setTitle(TITLE);

		builder.addSlot(new GuiElementBuilder(Items.ENDER_CHEST)
			.setName(RANDOM_KIT)
			.setCallback((index, type, action) -> {
				kitSelection.deselect(player);
				KitSelectionUi.playClickSound(player);
			}));

		for (KitType kitType : KitTypes.KITS) {
			KitSelectionUi.addKit(builder, kitSelection, player, kitType);
		}

		return builder.build(player);
	}

	private static void playClickSound(ServerPlayerEntity player) {
		player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 1);
	}
}
