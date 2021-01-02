package io.github.haykam821.cakewars.game.player;

import org.apache.commons.lang3.RandomStringUtils;

import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class TeamEntry {
	private final CakeWarsActivePhase phase;
	private final GameTeam gameTeam;
	private final Team scoreboardTeam;
	private final BlockBounds spawnBounds;
	private final BlockBounds generatorBounds;
	private final BlockBounds cakeBounds;
	private boolean cake = true;
	private int cakeEatCooldown = 0;
	private int generatorCooldown = 0;

	public TeamEntry(CakeWarsActivePhase phase, GameTeam gameTeam, MinecraftServer server, MapTemplate template) {
		this.phase = phase;
		this.gameTeam = gameTeam;

		ServerScoreboard scoreboard = server.getScoreboard();
		String key = RandomStringUtils.randomAlphanumeric(16);
		this.scoreboardTeam = TeamEntry.getOrCreateScoreboardTeam(key, scoreboard);
		this.initializeTeam();

		this.spawnBounds = this.getBoundsOrDefault(template, "spawn");
		this.generatorBounds = this.getBoundsOrDefault(template, "generator");
		this.cakeBounds = this.getBoundsOrDefault(template, "cake");
	}

	public GameTeam getGameTeam() {
		return this.gameTeam;
	}

	public Team getScoreboardTeam() {
		return this.scoreboardTeam;
	}

	public BlockBounds getSpawnBounds() {
		return this.spawnBounds;
	}

	public BlockBounds getGeneratorBounds() {
		return this.generatorBounds;
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
	}

	private Text getCakeEatenText(Text eaterName) {
		return new TranslatableText("text.cakewars.cake_eaten", this.getName(), eaterName).formatted(Formatting.RED);
	}

	public void resetCakeEatCooldown() {
		this.cakeEatCooldown = this.phase.getConfig().getCakeEatCooldown();
	}

	private void spawnGeneratorItem(ItemConvertible item) {
		ItemStack stack = new ItemStack(item, 1);

		boolean inserted = false;
		for (PlayerEntry player : this.phase.getPlayers()) {
			if (this.generatorBounds.contains(player.getPlayer().getBlockPos())) {
				player.getPlayer().giveItemStack(stack.copy());
			}
		}

		if (!inserted) {
			Vec3d centerPos = this.generatorBounds.getCenter();
			ServerWorld world = this.phase.getGameSpace().getWorld();

			ItemEntity itemEntity = new ItemEntity(world, centerPos.getX(), this.generatorBounds.getMin().getY(), centerPos.getZ(), stack);
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
			this.generatorCooldown = this.phase.getConfig().getGeneratorCooldown();
			this.spawnGeneratorItem(Items.BRICK);
		}
	}

	public boolean canEatCake() {
		return this.cakeEatCooldown == 0;
	}

	public Text getName() {
		return new LiteralText(this.gameTeam.getDisplay()).formatted(this.gameTeam.getFormatting());
	}

	private Text getUncoloredName() {
		return new LiteralText(this.gameTeam.getDisplay());
	}

	public void sendMessage(Text message) {
		for (PlayerEntry player : this.phase.getPlayers()) {
			if (player.getTeam() == this) {
				player.getPlayer().sendMessage(message, false);
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

	private void initializeTeam() {
		// Display
		this.scoreboardTeam.setDisplayName(this.getUncoloredName());
		this.scoreboardTeam.setColor(this.gameTeam.getFormatting());

		// Rules
		this.scoreboardTeam.setFriendlyFireAllowed(false);
		this.scoreboardTeam.setShowFriendlyInvisibles(true);
		this.scoreboardTeam.setCollisionRule(Team.CollisionRule.PUSH_OTHER_TEAMS);
	}

	private static Team getOrCreateScoreboardTeam(String key, ServerScoreboard scoreboard) {
		Team scoreboardTeam = scoreboard.getTeam(key);
		if (scoreboardTeam == null) {
			return scoreboard.addTeam(key);
		}
		return scoreboardTeam;
	}

	private BlockBounds getBoundsOrDefault(MapTemplate template, String key) {
		BlockBounds bounds = template.getMetadata().getFirstRegionBounds(this.gameTeam.getKey() + "_" + key);
		return bounds == null ? BlockBounds.EMPTY : bounds;
	}
}
