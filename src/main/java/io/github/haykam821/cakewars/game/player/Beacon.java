package io.github.haykam821.cakewars.game.player;

import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.player.team.TeamEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public class Beacon {
	private final CakeWarsActivePhase phase;
	private final TemplateRegion region;
	private final Text name;
	private final Item item;
	private final int maxHealth;
	private final int maxGeneratorCooldown;
	private int health;
	private int generatorCooldown;
	private TeamEntry controller = null;

	public Beacon(CakeWarsActivePhase phase, TemplateRegion region, Item item, int maxGeneratorCooldown) {
		this.phase = phase;
		this.region = region;
		this.name = Beacon.createHoverableName(region.getData(), item, maxGeneratorCooldown);
		this.item = item;

		this.maxHealth = this.phase.getConfig().getMaxBeaconHealth();
		this.health = this.maxHealth;

		this.maxGeneratorCooldown = maxGeneratorCooldown;
		this.generatorCooldown = this.maxGeneratorCooldown;
	}

	/**
	 * Determines whether a block at a given position should be turned into stained glass or wool when the beacon's controller is changed.
	 */
	private boolean isUnreplaceableBlock(BlockPos pos, BlockState state) {
		if (state.isOf(Blocks.IRON_BLOCK)) return true;
		if (state.isOf(Blocks.BEACON)) return true;
		if (state.isAir()) return true;
		if (!this.phase.getMap().isInitialBlock(pos)) return true;

		return false;
	}

	public Block getBlock(BlockPos pos, BlockState state, int minY) {
		if (this.isUnreplaceableBlock(pos, state)) return null;

		DyeColor dye = this.controller.getGameTeam().getDye();
		return pos.getY() == minY ? ColoredBlocks.wool(dye) : ColoredBlocks.glass(dye);
	}

	public void setController(TeamEntry controller) {
		Text controlChangedMessage = new TranslatableText("text.cakewars.beacon_control_changed", this.name, controller.getName()).formatted(Formatting.GOLD);
		controller.sendMessageIncludingSpectators(controlChangedMessage);
		if (this.controller != null) {
			this.controller.sendMessage(controlChangedMessage);
		}

		this.controller = controller;
		this.health = this.maxHealth;

		ServerWorld world = this.phase.getGameSpace().getWorld();
		int minY =  this.region.getBounds().getMin().getY();
		for (BlockPos pos : this.region.getBounds()) {
			BlockState state = world.getBlockState(pos);

			Block newBlock = this.getBlock(pos, state, minY);
			if (newBlock != null) {
				world.setBlockState(pos, newBlock.getDefaultState());
			}
		}
	}

	private boolean isStandingOnBeacon(PlayerEntry player) {
		return this.region.getBounds().contains(player.getPlayer().getBlockPos());
	}

	public void tick() {
		TeamEntry newController = null;
		for (PlayerEntry player : this.phase.getPlayers()) {
			if (this.isStandingOnBeacon(player)) {
				this.health += player.getTeam() == this.controller ? 1 : -1;
				newController = player.getTeam();
			}
		}

		this.health = Math.min(this.health, this.maxHealth);

		if (this.health < 0 && newController != null && this.controller != newController) {
			this.setController(newController);
		}

		if (this.controller != null) {
			this.generatorCooldown -= 1;
			if (this.generatorCooldown < 0) {
				this.generatorCooldown = this.maxGeneratorCooldown;
				this.controller.spawnGeneratorItem(this.item);
			}
		}
	}

	private static MutableText createName(CompoundTag data) {
		String name = data.getString("name");
		if (name == null) {
			return new TranslatableText("text.cakewars.unknown");
		}
		return new LiteralText(name);
	}

	private static Text createHoverableName(CompoundTag data, Item item, int maxGeneratorCooldown) {
		Text hoverText = new TranslatableText("text.cakewars.beacon_info", new TranslatableText(item.getTranslationKey()), maxGeneratorCooldown / 20).formatted(Formatting.GRAY);
		return Beacon.createName(data).styled(style -> {
			return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
		});
	}
}
