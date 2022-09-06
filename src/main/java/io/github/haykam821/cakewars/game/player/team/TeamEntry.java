package io.github.haykam821.cakewars.game.player.team;

import java.util.Iterator;

import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class TeamEntry {
	private static final BlockBounds DEFAULT_BOUNDS = BlockBounds.ofBlock(BlockPos.ORIGIN);

	private static final Formatting HAS_PLAYERS_FORMATTING = Formatting.GREEN;
	private static final Text HAS_CAKE_ICON = new LiteralText("✔").formatted(TeamEntry.HAS_PLAYERS_FORMATTING, Formatting.BOLD);
	private static final Text NO_PLAYERS_ICON = new LiteralText("❌").formatted(Formatting.RED, Formatting.BOLD);

	private final CakeWarsActivePhase phase;
	private final GameTeamConfig config;
	private final TeamUpgrades upgrades = new TeamUpgrades();
	private final BlockBounds spawnBounds;
	private final BlockBounds generatorBounds;
	private final BlockBounds cakeBounds;
	private boolean cake = true;
	private int cakeEatCooldown = 0;
	private int generatorCooldown = 0;

	public TeamEntry(CakeWarsActivePhase phase, GameTeamConfig config, MapTemplate template, GameTeamKey key) {
		this.phase = phase;
		this.config = config;

		this.spawnBounds = this.getBoundsOrDefault(template, key, "spawn");
		this.generatorBounds = this.getBoundsOrDefault(template, key, "generator");
		this.cakeBounds = this.getBoundsOrDefault(template, key, "cake");
	}

	public ItemStack getHelmet() {
		return this.getArmorItem(Items.LEATHER_HELMET);
	}

	public ItemStack getChestplate() {
		return this.getArmorItem(Items.LEATHER_CHESTPLATE);
	}

	public ItemStack getLeggings() {
		return this.getArmorItem(Items.LEATHER_LEGGINGS);
	}

	public ItemStack getBoots() {
		return this.getArmorItem(Items.LEATHER_BOOTS);
	}

	private ItemStack getArmorItem(ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.setDyeColor(this.config.dyeColor().getRgb())
			.setUnbreakable()
			.build();
	}
	
	public GameTeamConfig getConfig() {
		return this.config;
	}

	public TeamUpgrades getUpgrades() {
		return this.upgrades;
	}

	public BlockBounds getSpawnBounds() {
		return this.spawnBounds;
	}

	public BlockBounds getCakeBounds() {
		return this.cakeBounds;
	}

	public boolean hasCake() {
		return this.cake;
	}

	public void removeCake(PlayerEntry eater) {
		this.cake = false;

		this.phase.pling();
		this.phase.getGameSpace().getPlayers().sendMessage(this.getCakeEatenText(eater.getPlayer().getDisplayName()));

		// Title
		Text title = new TranslatableText("text.cakewars.cake_eaten.title").formatted(this.config.chatFormatting()).formatted(Formatting.BOLD);
		Text subtitle = new TranslatableText("text.cakewars.cake_eaten.subtitle");

		Iterator<PlayerEntry> iterator = this.phase.getPlayers().iterator();

		while (iterator.hasNext()) {
			PlayerEntry player = iterator.next();

			if (player.getPlayer() == null) {
				player.eliminate(false);
				iterator.remove();
			} else if (this == player.getTeam() && player.getPlayer() != null) {
				player.sendPacket(new TitleFadeS2CPacket(10, 60, 10));
				player.sendPacket(new TitleS2CPacket(title));
				player.sendPacket(new SubtitleS2CPacket(subtitle));
			}
		}

		this.phase.getSidebar().update();
	}

	private Text getCakeEatenText(Text eaterName) {
		return new TranslatableText("text.cakewars.cake_eaten", this.getName(), eaterName).formatted(Formatting.RED);
	}

	public void resetCakeEatCooldown() {
		this.cakeEatCooldown = this.phase.getConfig().getCakeEatCooldown();
	}

	public void spawnGeneratorItem(ItemConvertible item) {
		ItemStack stack = new ItemStack(item, 1);

		boolean inserted = false;
		for (PlayerEntry entry : this.phase.getPlayers()) {
			ServerPlayerEntity player = entry.getPlayer();

			if (player != null && this.generatorBounds.contains(player.getBlockPos())) {
				player.giveItemStack(stack.copy());
				inserted = true;
			}
		}

		if (!inserted) {
			Vec3d centerPos = this.generatorBounds.center();
			ServerWorld world = this.phase.getWorld();

			ItemEntity itemEntity = new ItemEntity(world, centerPos.getX(), this.generatorBounds.min().getY(), centerPos.getZ(), stack);
			itemEntity.setVelocity(Vec3d.ZERO);
			itemEntity.setToDefaultPickupDelay();
			world.spawnEntity(itemEntity);
		}
	}

	public void tick() {
		if (this.cakeEatCooldown > 0) {
			this.cakeEatCooldown -= 1;
		}

		if (this.generatorCooldown > 0) {
			this.generatorCooldown -= 1;
		}
		if (this.generatorCooldown <= 0) {
			this.generatorCooldown = this.phase.getConfig().getBrickGeneratorCooldown();
			this.spawnGeneratorItem(Items.BRICK);
		}
	}

	public boolean canEatCake() {
		return this.cakeEatCooldown == 0;
	}

	public Text getSidebarEntry(int playerCount) {
		return new LiteralText("")
			.append(this.getSidebarEntryIcon(playerCount))
			.append(" ")
			.append(this.getName().shallowCopy().formatted(Formatting.BOLD));
	}

	public Text getSidebarEntryIcon(int playerCount) {
		if (this.cake) {
			return TeamEntry.HAS_CAKE_ICON;
		} else if (playerCount == 0) {
			return TeamEntry.NO_PLAYERS_ICON;
		} else {
			return new LiteralText("" + playerCount).formatted(TeamEntry.HAS_PLAYERS_FORMATTING);
		}
	}

	public Text getName() {
		return this.config.name();
	}

	public void sendMessage(Text message) {
		for (PlayerEntry entry : this.phase.getPlayers()) {
			ServerPlayerEntity player = entry.getPlayer();

			if (entry.getTeam() == this && player != null) {
				player.sendMessage(message, false);
			}
		}
	}

	public void sendMessageIncludingSpectators(Text message) {
		for (ServerPlayerEntity player : this.phase.getGameSpace().getPlayers()) {
			PlayerEntry entry = this.phase.getPlayerEntry(player);
			if (entry == null || entry.getTeam() == this) {
				player.sendMessage(message, false);
			}
		}
	}

	private BlockBounds getBoundsOrDefault(MapTemplate template, GameTeamKey key, String type) {
		BlockBounds bounds = template.getMetadata().getFirstRegionBounds(key.id() + "_" + type);
		return bounds == null ? DEFAULT_BOUNDS : bounds;
	}
}
