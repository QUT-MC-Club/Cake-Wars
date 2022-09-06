package io.github.haykam821.cakewars.game.player;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.haykam821.cakewars.game.item.RuneOfHoldingItem;
import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.player.kit.BuilderKit;
import io.github.haykam821.cakewars.game.player.kit.Kit;
import io.github.haykam821.cakewars.game.player.team.TeamEntry;
import io.github.haykam821.cakewars.game.shop.BrickShop;
import io.github.haykam821.cakewars.game.shop.EmeraldShop;
import io.github.haykam821.cakewars.game.shop.NetherStarShop;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class PlayerEntry {
	private static final ItemStack INITIAL_SWORD = ItemStackBuilder.of(Items.WOODEN_SWORD).setUnbreakable().build();

	private final CakeWarsActivePhase phase;
	private final ServerPlayerEntity player;
	private final TeamEntry team;
	private final Kit kit = new BuilderKit();
	private int respawnCooldown = -1;
	private int aliveTicks = 0;

	private PlayerInventory savedInventory;

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
		if (spectator && this.popRuneOfHolding()) {
			this.saveInventory();
		}

		this.player.getInventory().clear();
		if (this.player.currentScreenHandler != null) {
			this.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
		}

		this.player.setExperienceLevel(0);
		this.player.setExperiencePoints(0);

		if (spectator) {
			this.respawnCooldown = this.phase.getConfig().getRespawnCooldown();
		} else {
			this.player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 4, 100, true, false));

			if (this.savedInventory == null) {
				this.initializeInventory();
			} else {
				this.restoreInventory();
			}
		}
	}

	/**
	 * Decrements a rune of holding in the inventory, if present.
	 * @return whether a rune of holding was present in the inventory
	 */
	private boolean popRuneOfHolding() {
		// Prioritize runes of holding from the cursor stack
		ScreenHandler screenHandler = this.player.currentScreenHandler;

		if (screenHandler != null) {
			ItemStack cursorStack = screenHandler.getCursorStack();

			if (this.isRuneOfHolding(cursorStack)) {
				cursorStack.decrement(1);
				return true;
			}
		}

		// Check inventory for runes of holding
		PlayerInventory inventory = this.player.getInventory();
		int size = inventory.size();

		for (int slot = 0; slot < size; slot++) {
			ItemStack stack = inventory.getStack(slot);

			if (this.isRuneOfHolding(stack)) {
				stack.decrement(1);
				return true;
			}
		}

		return false;
	}

	private boolean isRuneOfHolding(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.getItem() instanceof RuneOfHoldingItem;
	}

	private void initializeInventory() {
		this.player.giveItemStack(this.team.getUpgrades().applyTo(INITIAL_SWORD.copy()));

		this.player.equipStack(EquipmentSlot.HEAD, this.team.getHelmet());
		this.player.equipStack(EquipmentSlot.CHEST, this.team.getChestplate());
		this.player.equipStack(EquipmentSlot.LEGS, this.team.getLeggings());
		this.player.equipStack(EquipmentSlot.FEET, this.team.getBoots());
	}

	private void saveInventory() {
		PlayerInventory inventory = this.player.getInventory();
		ScreenHandler screenHandler = this.player.currentScreenHandler;

		// Ensure that cursor stack is saved
		if (screenHandler != null) {
			inventory.offerOrDrop(screenHandler.getCursorStack());
		}

		this.savedInventory = new PlayerInventory(player);
		this.savedInventory.clone(inventory);
	}

	private void restoreInventory() {
		player.getInventory().clone(this.savedInventory);
		this.savedInventory = null;
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
		} else {
			this.aliveTicks += 1;
			this.kit.tick(this, this.aliveTicks);
		}

		return false;
	}

	public boolean hasLessThan(ItemConvertible item, int maxCount) {
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
