package io.github.haykam821.cakewars.game.player;

import io.github.haykam821.cakewars.game.map.CakeWarsMap;
import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.player.team.TeamEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.WorldEvents;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public class Beacon {
	private static final double PROGRESS_VARIANCE = 1 / 3d;

	private final CakeWarsActivePhase phase;
	private final TemplateRegion region;
	private final BlockBounds colorizeBounds;
	private final Text name;
	private final Item item;
	private final int maxHealth;
	private final int maxGeneratorCooldown;
	private final long seed = RandomSeed.getSeed();
	private int health;
	private int generatorCooldown;
	private TeamEntry controller = null;
	private TeamEntry contestant = null;

	public Beacon(CakeWarsActivePhase phase, TemplateRegion region, Item item, int maxGeneratorCooldown) {
		this.phase = phase;

		this.region = region;
		this.colorizeBounds = Beacon.getColorizeBounds(phase.getMap(), region);

		this.name = Beacon.createHoverableName(region.getData(), item, maxGeneratorCooldown);
		this.item = item;

		this.maxHealth = this.phase.getConfig().getMaxBeaconHealth();
		this.health = this.maxHealth / 2;

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
		if (!this.phase.getMap().isProtected(pos)) return true;

		return false;
	}

	private DyeColor getColor(Random random, double progress) {
		double value = random.nextDouble() * PROGRESS_VARIANCE;

		if (this.controller != null && progress > value + PROGRESS_VARIANCE) {
			return this.controller.getConfig().blockDyeColor();
		} else if (this.contestant != null && progress < value) {
			return this.contestant.getConfig().blockDyeColor();
		}

		return DyeColor.WHITE;
	}

	public Block getBlock(BlockPos pos, BlockState state, DyeColor dye, boolean floor) {
		if (this.isUnreplaceableBlock(pos, state)) return null;
		return floor ? ColoredBlocks.wool(dye) : ColoredBlocks.glass(dye);
	}

	public void setController(TeamEntry controller) {
		// Send messages
		Text controlChangedMessage = Text.translatable("text.cakewars.beacon_control_changed", this.name, controller.getName()).formatted(Formatting.GOLD);
		controller.sendMessageIncludingSpectators(controlChangedMessage);
		if (this.controller != null) {
			this.controller.sendMessage(controlChangedMessage);
		}

		// Change contestant team to controller
		this.controller = controller;
		this.contestant = null;

		this.health = this.maxHealth;
	}

	public void updateBlocks() {
		ServerWorld world = this.phase.getWorld();

		BlockPos min = this.colorizeBounds.min();
		BlockPos max = this.colorizeBounds.max();

		Random random = Random.create(this.seed);
		double progress = this.health / (double) this.maxHealth;

		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int x = min.getX(); x <= max.getX(); x++) {
			for (int z = min.getZ(); z <= max.getZ(); z++) {
				DyeColor dye = this.getColor(random, progress);

				for (int y = min.getY(); y <= max.getY(); y++) {
					pos.set(x, y, z);
					BlockState state = world.getBlockState(pos);

					boolean floor = y == min.getY();
					Block newBlock = this.getBlock(pos, state, dye, floor);

					if (newBlock != null) {
						BlockState newState = newBlock.getDefaultState();

						if (state != newState) {
							world.setBlockState(pos, newState);

							if (floor) {
								world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(newState));
							}
						}
					}
				}
			}
		}
	}

	private boolean isStandingOnBeacon(PlayerEntry entry) {
		ServerPlayerEntity player = entry.getPlayer();
		return player != null && this.region.getBounds().contains(player.getBlockPos());
	}

	private boolean hasOverHalfHealth() {
		return this.health >= this.maxHealth / 2;
	}

	public void tick() {
		this.tickCapture();
		this.tickGeneration();
	}

	private void tickCapture() {
		TeamEntry team = null;
		int multiplier = 0;

		for (PlayerEntry player : this.phase.getPlayers()) {
			if (this.isStandingOnBeacon(player)) {
				if (team == null) {
					team = player.getTeam();
				} else if (team != player.getTeam()) {
					// Multiple teams on beacon prevents capture change
					return;
				}

				multiplier += 1;
			}
		}

		double previousHealth = this.health;

		if (team != null && team == this.controller) {
			// Revert back to controller based on multiplier
			this.health += multiplier;
		} else if (team != null && (team == this.contestant || this.hasOverHalfHealth())) {
			// Progress towards contestant based on multiplier
			this.contestant = team;
			this.health -= multiplier;
		} else if (this.controller != null || !this.hasOverHalfHealth()) {
			// Revert slowly back to controller or neutral
			this.health += 1;
		}

		this.health = MathHelper.clamp(this.health, 0, this.maxHealth);
		
		if (this.health == 0) {
			this.setController(this.contestant);
		}

		if (this.health != previousHealth) {
			this.updateBlocks();
		}
	}

	private void tickGeneration() {
		if (this.controller != null) {
			this.generatorCooldown -= 1;
			if (this.generatorCooldown < 0) {
				this.generatorCooldown = this.maxGeneratorCooldown;
				this.controller.spawnGeneratorItem(this.item);
			}
		}
	}

	/**
	 * Gets the bounds of the colorize region specified in the given region's data.
	 * 
	 * <p>Falls back to the bounds of the given region if the colorize region does not exist or is not specified.
	 */
	private static BlockBounds getColorizeBounds(CakeWarsMap map, TemplateRegion region) {
		String marker = region.getData().getString("ColorizeRegion");
		if (marker != null) {
			BlockBounds colorizeBounds = map.getTemplate().getMetadata().getFirstRegionBounds(marker);
			if (colorizeBounds != null) return colorizeBounds;
		}

		return region.getBounds();
	}

	private static MutableText createName(NbtCompound data) {
		String name = data.getString("name");
		if (name == null) {
			return Text.translatable("text.cakewars.unknown");
		}
		return Text.literal(name);
	}

	private static Text createHoverableName(NbtCompound data, Item item, int maxGeneratorCooldown) {
		Text hoverText = Text.translatable("text.cakewars.beacon_info", Text.translatable(item.getTranslationKey()), maxGeneratorCooldown / 20).formatted(Formatting.GRAY);
		return Beacon.createName(data).styled(style -> {
			return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
		});
	}
}
