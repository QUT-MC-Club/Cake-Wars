package io.github.haykam821.cakewars.game.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.pb4.sgui.api.gui.BaseSlotGui;
import io.github.haykam821.cakewars.game.item.RuneOfHoldingItem;
import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.player.kit.Kit;
import io.github.haykam821.cakewars.game.player.kit.KitType;
import io.github.haykam821.cakewars.game.player.team.TeamEntry;
import io.github.haykam821.cakewars.game.shop.Shop;
import io.github.haykam821.cakewars.game.shop.Shops;
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
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
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

	private static final Text CANNOT_EAT_OWN_CAKE = new TranslatableText("text.cakewars.cannot_eat_own_cake").formatted(Formatting.RED);
	private static final Text CANNOT_OPEN_TEAM_CHEST = new TranslatableText("text.cakewars.cannot_open_team_chest").formatted(Formatting.RED);

	private final CakeWarsActivePhase phase;
	private ServerPlayerEntity player;
	private final UUID uuid;
	private final TeamEntry team;
	private final Kit kit;
	private int respawnCooldown = -1;
	private int aliveTicks = 0;

	private PlayerInventory savedInventory;
	private final List<ItemStack> savedKitStacks = new ArrayList<>();

	private Shop shop;
	private BaseSlotGui shopGui;

	public PlayerEntry(CakeWarsActivePhase phase, ServerPlayerEntity player, TeamEntry team, KitType kitType) {
		this.phase = phase;
		this.player = player;
		this.uuid = player.getUuid();
		this.team = team;
		this.kit = kitType.create(this);
	}

	// Listeners
	public ActionResult onDeath(DamageSource source) {
		if (this.isAlive()) {
			PlayerEntry attacker = null;

			if (source.getAttacker() instanceof ServerPlayerEntity) {
				attacker = this.phase.getPlayerEntry((ServerPlayerEntity) source.getAttacker());

				if (attacker != null && attacker.isAlive()) {
					attacker.kit.onKill();
				}
			}

			if (this.team.hasCake()) {
				this.spawn(true, true);

				Text deathMessage = source.getDeathMessage(this.getPlayer()).shallowCopy().formatted(Formatting.RED);
				this.team.sendMessageIncludingSpectators(deathMessage);

				if (attacker != null && this.team != attacker.getTeam()) {
					attacker.getTeam().sendMessage(deathMessage);
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
		BlockPos pos = hitResult.getBlockPos();

		if (hand == Hand.MAIN_HAND && this.isAlive()) {
			ServerWorld world = this.getPlayer().getWorld();

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

		for (TeamEntry team : this.phase.getTeams()) {
			if (team.isTeamChestInaccessible(this, pos)) {
				if (this.isAlive()) {
					player.sendMessage(CANNOT_OPEN_TEAM_CHEST, true);
				}

				return ActionResult.FAIL;
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
						this.openShop(Shops.BRICK);
						return ActionResult.FAIL;
					}
				}
				for (TemplateRegion emeraldShop : this.getRegions("emerald_villager")) {
					if (emeraldShop.getBounds().contains(villager.getBlockPos())) {
						this.openShop(Shops.EMERALD);
						return ActionResult.FAIL;
					}
				}
				for (TemplateRegion emeraldShop : this.getRegions("nether_star_villager")) {
					if (emeraldShop.getBounds().contains(villager.getBlockPos())) {
						this.openShop(Shops.NETHER_STAR);
						return ActionResult.FAIL;
					}
				}
			}
		}
		return ActionResult.PASS;
	}

	public ActionResult onProjectileHitBlock(ProjectileEntity entity, BlockHitResult hitResult) {
		if (this.canProjectileBreakBlock(entity, hitResult)) {
			entity.getWorld().breakBlock(hitResult.getBlockPos(), false, entity);
			entity.discard();
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
	private void openShop(Shop shop) {
		BaseSlotGui gui = shop.build(this);
		gui.open();

		this.shop = shop;
		this.shopGui = gui;
	}

	private void closeShop() {
		if (this.shopGui != null && this.shopGui.isOpen()) {
			this.shopGui.close();
		}

		this.shop = null;
		this.shopGui = null;
	}

	public void refreshShop() {
		if (this.shop != null) {
			this.shop.update(this, this.shopGui, true);
		}
	}

	private boolean canProjectileBreakBlock(ProjectileEntity entity, BlockHitResult hitResult) {
		BlockPos pos = hitResult.getBlockPos();
		if (this.getPhase().getMap().isProtected(pos)) return false;

		if (!(entity instanceof PersistentProjectileEntity)) return false;
		if (!this.kit.canProjectileBreakBlock((PersistentProjectileEntity) entity, hitResult)) return false;

		return true;
	}

	private Set<TemplateRegion> getRegions(String key) {
		return this.phase.getMap().getTemplate().getMetadata().getRegions(key).collect(Collectors.toSet());
	}
	
	private void eatCake(ServerWorld world, BlockPos pos, BlockState state, Hand hand, TeamEntry team) {
		ServerPlayerEntity player = this.getPlayer();

		ItemStack stack = player.getStackInHand(hand);
		if (stack.getItem() instanceof BlockItem) return;

		if (!team.canEatCake()) return;
		if (!this.phase.getConfig().shouldAllowSelfEating() && team == this.team) {
			player.sendMessage(CANNOT_EAT_OWN_CAKE, true);
			return;
		}

		int bites = state.get(Properties.BITES) + 1;
		if (bites > 6) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			this.phase.getMap().removeProtection(pos);

			team.removeCake(this);
		} else {
			world.setBlockState(pos, state.with(Properties.BITES, bites));
		}

		player.swingHand(Hand.MAIN_HAND);
		world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, 1, 1);

		team.resetCakeEatCooldown();
	}

	public Vec3d getSpawnPos() {
		BlockBounds teamSpawn = this.team.getSpawnBounds();
		return teamSpawn.centerBottom();
	}

	private void teleportToSpawn() {
		ServerPlayerEntity player = this.getPlayer();
		Vec3d pos = this.getSpawnPos();

		player.teleport(player.getWorld(), pos.getX(), pos.getY(), pos.getZ(), 0, 0);
	}

	public void spawn(boolean spectator, boolean teleport) {
		ServerPlayerEntity player = this.getPlayer();

		// State
		player.changeGameMode(spectator ? GameMode.SPECTATOR : GameMode.SURVIVAL);
		player.setHealth(player.getMaxHealth());
		player.setAir(player.getMaxAir());
		player.setFireTicks(0);
		player.fallDistance = 0;
		player.clearStatusEffects();
		this.aliveTicks = 0;

		// Position
		if (teleport) {
			this.teleportToSpawn();
		}

		// Inventory
		if (spectator && this.savedInventory == null) {
			this.saveInventory();
		}

		player.getInventory().clear();
		if (player.currentScreenHandler != null) {
			player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
		}

		player.setExperienceLevel(0);
		player.setExperiencePoints(0);

		if (spectator) {
			this.respawnCooldown = this.phase.getConfig().getRespawnCooldown();
		} else {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 4, 100, true, false));

			if (this.savedInventory == null) {
				this.initializeInventory();
			} else {
				this.restoreInventory();
			}
		}

		this.closeShop();
	}

	public void detach() {
		this.spawn(true, false);
		this.player = null;
	}

	public boolean reattach(ServerPlayerEntity player) {
		if (this.uuid.equals(player.getUuid())) {
			this.player = player;
			return true;
		}

		return false;
	}

	/**
	 * Decrements a rune of holding in the inventory, if present.
	 * @return whether a rune of holding was present in the inventory
	 */
	private boolean popRuneOfHolding() {
		// Prioritize runes of holding from the cursor stack
		ServerPlayerEntity player = this.getPlayer();
		ScreenHandler screenHandler = player.currentScreenHandler;

		if (screenHandler != null) {
			ItemStack cursorStack = screenHandler.getCursorStack();

			if (this.isRuneOfHolding(cursorStack)) {
				cursorStack.decrement(1);
				return true;
			}
		}

		// Check inventory for runes of holding
		PlayerInventory inventory = player.getInventory();
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
		ServerPlayerEntity player = this.getPlayer();

		player.giveItemStack(this.team.getUpgrades().applyTo(INITIAL_SWORD.copy()));

		player.equipStack(EquipmentSlot.HEAD, this.team.getHelmet());
		player.equipStack(EquipmentSlot.CHEST, this.team.getChestplate());
		player.equipStack(EquipmentSlot.LEGS, this.team.getLeggings());
		player.equipStack(EquipmentSlot.FEET, this.team.getBoots());
		
		for (ItemStack stack : this.savedKitStacks) {
			player.giveItemStack(stack);
		}

		this.savedKitStacks.clear();
	}

	private void saveInventory() {
		ServerPlayerEntity player = this.getPlayer();

		PlayerInventory inventory = player.getInventory();
		ScreenHandler screenHandler = player.currentScreenHandler;

		// Ensure that cursor stack is saved
		if (screenHandler != null) {
			inventory.offerOrDrop(screenHandler.getCursorStack());
		}

		if (this.popRuneOfHolding()) {
			this.savedInventory = new PlayerInventory(player);
			this.savedInventory.clone(inventory);
		} else {
			for (int slot = 0; slot < inventory.size(); slot++) {
				ItemStack stack = inventory.getStack(slot);

				if (this.kit.canKeepAfterRespawn(stack)) {
					this.savedKitStacks.add(stack);
				}
			}
		}
	}

	private void restoreInventory() {
		ServerPlayerEntity player = this.getPlayer();

		player.getInventory().clone(this.savedInventory);
		this.savedInventory = null;
	}

	public boolean tick() {
		ServerPlayerEntity player = this.getPlayer();
		if (player == null) return false;

		if (player.getY() < this.phase.getMinY()) {
			// Since this can result in elimination, it must be checked to prevent a ConcurrentModificationException
			if (!this.team.hasCake()) {
				this.eliminate(false);
				return true;
			} else {
				player.damage(DamageSource.OUT_OF_WORLD, Integer.MAX_VALUE);
			}
		}

		if (this.respawnCooldown > -1) {
			if (this.respawnCooldown > 0 && this.respawnCooldown % 20 == 0) {
				player.sendMessage(new TranslatableText("text.cakewars.respawning", this.respawnCooldown / 20), true);
			} else if (this.respawnCooldown == 0) {
				this.spawn(false, true);
			}
			this.respawnCooldown -= 1;
		} else {
			this.aliveTicks += 1;
			this.kit.tick(this.aliveTicks);
		}

		if (this.shopGui != null && !this.shopGui.isOpen()) {
			this.closeShop();
		}

		return false;
	}

	public boolean hasLessThan(ItemConvertible item, int maxCount) {
		ServerPlayerEntity player = this.getPlayer();
		return player.getInventory().count(item.asItem()) < maxCount;
	}

	private void updateInventory() {
		ServerPlayerEntity player = this.getPlayer();

		player.currentScreenHandler.sendContentUpdates();
		player.playerScreenHandler.onContentChanged(player.getInventory());
	}

	public void applyUpgrades() {
		ServerPlayerEntity player = this.getPlayer();

		for (int slot = 0; slot < player.getInventory().size(); slot++) {
			this.team.getUpgrades().applyTo(player.getInventory().getStack(slot));
		}
		this.updateInventory();
	}

	public void eliminate(boolean remove) {
		ServerPlayerEntity player = this.getPlayer();

		if (player != null) {
			this.phase.getGameSpace().getPlayers().sendMessage(this.getEliminationMessage());
			this.getPlayer().changeGameMode(GameMode.SPECTATOR);
		}

		if (remove) {
			this.phase.getPlayers().remove(this);
			this.phase.getSidebar().update();
		}
	}

	public void sendPacket(Packet<?> packet) {
		ServerPlayerEntity player = this.getPlayer();
		player.networkHandler.sendPacket(packet);
	}

	public Text getEliminationMessage() {
		ServerPlayerEntity player = this.getPlayer();
		return new TranslatableText("text.cakewars.eliminated", player.getDisplayName()).formatted(Formatting.RED);
	}
}
