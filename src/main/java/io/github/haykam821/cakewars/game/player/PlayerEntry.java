package io.github.haykam821.cakewars.game.player;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.haykam821.cakewars.game.item.DeployPlatformItem;
import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.player.team.TeamEntry;
import io.github.haykam821.cakewars.game.shop.BrickShop;
import io.github.haykam821.cakewars.game.shop.EmeraldShop;
import io.github.haykam821.cakewars.game.shop.NetherStarShop;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class PlayerEntry {
	private static final ItemStack INITIAL_SWORD = ItemStackBuilder.of(Items.WOODEN_SWORD).setUnbreakable().build();

	private final CakeWarsActivePhase phase;
	private final ServerPlayerEntity player;
	private final TeamEntry team;
	private final Kit kit = Kit.BUILDER;
	private int respawnCooldown = -1;
	private int aliveTicks = 0;

	public PlayerEntry(CakeWarsActivePhase phase, ServerPlayerEntity player, TeamEntry team) {
		this.phase = phase;
		this.player = player;
		this.team = team;
	}

	// Listeners
	public ActionResult onDeath(DamageSource source) {
		if (this.isAlive()) {
			if (this.team.hasCake()) {
				this.spawn(true);

				Text deathMessage = source.getDeathMessage(this.player).shallowCopy().formatted(Formatting.RED);
				this.team.sendMessageIncludingSpectators(deathMessage);

				if (source.getAttacker() instanceof ServerPlayerEntity) {
					PlayerEntry attackerEntry = this.phase.getPlayerEntry((ServerPlayerEntity) source.getAttacker());
					if (attackerEntry != null && this.team != attackerEntry.getTeam()) {
						attackerEntry.getTeam().sendMessage(deathMessage);
					}
				}
			} else {
				this.eliminate(true);
			}
		} else {
			this.teleportToSpawn();
		}
		return ActionResult.FAIL;
	}

	public ActionResult onUseBlock(Hand hand, BlockHitResult hitResult) {
		if (hand == Hand.MAIN_HAND && this.isAlive()) {
			ServerWorld world = this.player.getWorld();
			BlockPos pos = hitResult.getBlockPos();

			BlockState state = world.getBlockState(pos);
			if (state.isOf(Blocks.CAKE)) {
				for (TeamEntry team : this.phase.getTeams()) {
					if (team.hasCake() && team.getCakeBounds().contains(pos)) {
						this.eatCake(world, pos, state, hand, team);
						return ActionResult.FAIL;
					}
				}
			}
		}

		return ActionResult.PASS;
	}

	public ActionResult onUseEntity(PlayerEntity player, Entity entity) {
		if (this.isAlive() && entity instanceof VillagerEntity) {
			VillagerEntity villager = (VillagerEntity) entity;
			if (villager.isAiDisabled()) {
				for (TemplateRegion brickShop : this.getRegions("brick_villager")) {
					if (brickShop.getBounds().contains(villager.getBlockPos())) {
						BrickShop.build(this).open();
						return ActionResult.FAIL;
					}
				}
				for (TemplateRegion emeraldShop : this.getRegions("emerald_villager")) {
					if (emeraldShop.getBounds().contains(villager.getBlockPos())) {
						EmeraldShop.build(this).open();
						return ActionResult.FAIL;
					}
				}
				for (TemplateRegion emeraldShop : this.getRegions("nether_star_villager")) {
					if (emeraldShop.getBounds().contains(villager.getBlockPos())) {
						NetherStarShop.build(this).open();
						return ActionResult.FAIL;
					}
				}
			}
		}
		return ActionResult.PASS;
	}

	// Getters
	public CakeWarsActivePhase getPhase() {
		return this.phase;
	}

	public ServerPlayerEntity getPlayer() {
		return this.player;
	}

	public TeamEntry getTeam() {
		return this.team;
	}

	public boolean isAlive() {
		return this.respawnCooldown == -1;
	}

	// Utilities
	private Set<TemplateRegion> getRegions(String key) {
		return this.phase.getMap().getTemplate().getMetadata().getRegions(key).collect(Collectors.toSet());
	}
	
	private void eatCake(ServerWorld world, BlockPos pos, BlockState state, Hand hand, TeamEntry team) {
		ItemStack stack = this.player.getStackInHand(hand);
		if (stack.getItem() instanceof BlockItem) return;

		if (!team.canEatCake()) return;
		if (!this.phase.getConfig().shouldAllowSelfEating() && team == this.team) {
			this.player.sendMessage(new TranslatableText("text.cakewars.cannot_eat_own_cake").formatted(Formatting.RED), false);
			return;
		}

		int bites = state.get(Properties.BITES) + 1;
		if (bites > 6) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			this.phase.getMap().removeInitialBlock(pos);

			team.removeCake(this);
		} else {
			world.setBlockState(pos, state.with(Properties.BITES, bites));
		}

		this.getPlayer().swingHand(Hand.MAIN_HAND);
		world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, 1, 1);

		team.resetCakeEatCooldown();
	}

	private void teleportToSpawn() {
		BlockBounds teamSpawn = this.team.getSpawnBounds();
		Vec3d teamSpawnCenter = teamSpawn.center();
		this.player.teleport(this.player.getWorld(), teamSpawnCenter.getX(), teamSpawn.min().getY(), teamSpawnCenter.getZ(), 0, 0);
	}

	public void spawn(boolean spectator) {
		// State
		this.player.changeGameMode(spectator ? GameMode.SPECTATOR : GameMode.SURVIVAL);
		this.player.setHealth(this.player.getMaxHealth());
		this.player.setAir(this.player.getMaxAir());
		this.player.setFireTicks(0);
		this.player.fallDistance = 0;
		this.player.clearStatusEffects();
		this.aliveTicks = 0;

		// Position
		this.teleportToSpawn();

		// Inventory
		this.player.getInventory().clear();
		this.player.setExperienceLevel(0);
		this.player.setExperiencePoints(0);

		if (spectator) {
			this.respawnCooldown = this.phase.getConfig().getRespawnCooldown();
		} else {
			this.player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 4, 100, true, false));
			this.player.giveItemStack(this.team.getUpgrades().applyTo(INITIAL_SWORD.copy()));

			this.player.equipStack(EquipmentSlot.HEAD, this.team.getHelmet());
			this.player.equipStack(EquipmentSlot.CHEST, this.team.getChestplate());
			this.player.equipStack(EquipmentSlot.LEGS, this.team.getLeggings());
			this.player.equipStack(EquipmentSlot.FEET, this.team.getBoots());
		}
	}

	public boolean tick() {
		if (this.player.getY() < this.phase.getMinY()) {
			// Since this can result in elimination, it must be checked to prevent a ConcurrentModificationException
			if (!this.team.hasCake()) {
				this.eliminate(false);
				return true;
			} else {
				this.player.damage(DamageSource.OUT_OF_WORLD, Integer.MAX_VALUE);
			}
		}

		if (this.respawnCooldown > -1) {
			if (this.respawnCooldown > 0 && this.respawnCooldown % 20 == 0) {
				this.player.sendMessage(new TranslatableText("text.cakewars.respawning", this.respawnCooldown / 20), true);
			} else if (this.respawnCooldown == 0) {
				this.spawn(false);
			}
			this.respawnCooldown -= 1;
		}

		this.aliveTicks += 1;
		if (this.kit == Kit.BUILDER) {
			DyeColor dye = this.team.getConfig().blockDyeColor();
			if (this.aliveTicks % 80 == 0) {
				Block wool = ColoredBlocks.wool(dye);
				if (this.hasLessThan(wool, 32)) {
					this.player.giveItemStack(new ItemStack(wool));
				}
			}
			if (this.aliveTicks % 200 == 0) {
				Item deployPlatform = DeployPlatformItem.ofDyeColor(dye);
				if (this.hasLessThan(deployPlatform, 5)) {
					this.player.giveItemStack(new ItemStack(deployPlatform));
				}
			}
		}

		return false;
	}

	private boolean hasLessThan(ItemConvertible item, int maxCount) {
		return this.player.getInventory().count(item.asItem()) < maxCount;
	}

	private void updateInventory() {
		this.player.currentScreenHandler.sendContentUpdates();
		this.player.playerScreenHandler.onContentChanged(this.player.getInventory());
	}

	public void applyUpgrades() {
		for (int slot = 0; slot < this.player.getInventory().size(); slot++) {
			this.team.getUpgrades().applyTo(this.player.getInventory().getStack(slot));
		}
		this.updateInventory();
	}

	public void eliminate(boolean remove) {
		this.phase.getGameSpace().getPlayers().sendMessage(this.getEliminationMessage());

		if (remove) {
			this.phase.getPlayers().remove(this);
			this.phase.getSidebar().update();
		}
		this.getPlayer().changeGameMode(GameMode.SPECTATOR);
	}

	public void sendPacket(Packet<?> packet) {
		this.player.networkHandler.sendPacket(packet);
	}

	public Text getEliminationMessage() {
		return new TranslatableText("text.cakewars.eliminated", this.player.getDisplayName()).formatted(Formatting.RED);
	}
}
