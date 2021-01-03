package io.github.haykam821.cakewars.game.player;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.haykam821.cakewars.game.event.UseEntityListener;
import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.shop.BrickShop;
import io.github.haykam821.cakewars.game.shop.EmeraldShop;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class PlayerEntry implements PlayerDeathListener, UseBlockListener, UseEntityListener {
	private static final ItemStack INITIAL_SWORD = ItemStackBuilder.of(Items.WOODEN_SWORD).setUnbreakable().build();

	private final CakeWarsActivePhase phase;
	private final ServerPlayerEntity player;
	private final TeamEntry team;
	private int respawnCooldown = -1;

	public PlayerEntry(CakeWarsActivePhase phase, ServerPlayerEntity player, TeamEntry team) {
		this.phase = phase;
		this.player = player;
		this.team = team;
	}

	// Listeners
	@Override
	public ActionResult onDeath(ServerPlayerEntity player, DamageSource source) {
		if (this.team.hasCake()) {
			this.spawn(true);

			Text deathMessage = source.getDeathMessage(player).shallowCopy().formatted(Formatting.RED);
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
		return ActionResult.FAIL;
	}

	@Override
	public ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		ServerWorld world = this.player.getServerWorld();
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

		return ActionResult.PASS;
	}

	@Override
	public ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
		if (entity instanceof VillagerEntity) {
			VillagerEntity villager = (VillagerEntity) entity;
			if (villager.isAiDisabled()) {
				for (TemplateRegion brickShop : this.getRegions("brick_villager")) {
					if (brickShop.getBounds().contains(villager.getBlockPos())) {
						player.openHandledScreen(BrickShop.build(this));
						return ActionResult.FAIL;
					}
				}
				for (TemplateRegion emeraldShop : this.getRegions("emerald_villager")) {
					if (emeraldShop.getBounds().contains(villager.getBlockPos())) {
						player.openHandledScreen(EmeraldShop.build(this));
						return ActionResult.FAIL;
					}
				}
			}
		}
		return ActionResult.PASS;
	}

	// Getters
	public ServerPlayerEntity getPlayer() {
		return this.player;
	}

	public TeamEntry getTeam() {
		return this.team;
	}

	// Utilities
	private Set<TemplateRegion> getRegions(String key) {
		return this.phase.getMap().getTemplate().getMetadata().getRegions(key).collect(Collectors.toSet());
	}
	
	private void eatCake(ServerWorld world, BlockPos pos, BlockState state, Hand hand, TeamEntry team) {
		ItemStack stack = this.player.getStackInHand(hand);
		if (stack.getItem() instanceof BlockItem) return;

		if (!team.canEatCake()) return;
		if (team == this.team) {
			this.player.sendMessage(new TranslatableText("text.cakewars.cannot_eat_own_cake").formatted(Formatting.RED), false);
			return;
		}

		int bites = state.get(Properties.BITES) + 1;
		if (bites > 6) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			team.removeCake(this);
		} else {
			world.setBlockState(pos, state.with(Properties.BITES, bites));
		}

		this.getPlayer().swingHand(Hand.MAIN_HAND);
		world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, 1, 1);

		team.resetCakeEatCooldown();
	}

	public void spawn(boolean spectator) {
		// State
		this.player.setGameMode(spectator ? GameMode.SPECTATOR : GameMode.SURVIVAL);
		this.player.setHealth(this.player.getMaxHealth());
		this.player.setAir(this.player.getMaxAir());
		this.player.setFireTicks(0);
		this.player.fallDistance = 0;
		this.player.clearStatusEffects();

		// Position
		BlockBounds teamSpawn = this.team.getSpawnBounds();
		Vec3d teamSpawnCenter = teamSpawn.getCenter();
		this.player.teleport(this.player.getServerWorld(), teamSpawnCenter.getX(), teamSpawn.getMin().getY(), teamSpawnCenter.getZ(), 0, 0);

		// Inventory
		this.player.inventory.clear();
		this.player.setExperienceLevel(0);
		this.player.setExperiencePoints(0);

		if (spectator) {
			this.respawnCooldown = this.phase.getConfig().getRespawnCooldown();
		} else {
			this.player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 4, 100, true, false));
			this.player.giveItemStack(INITIAL_SWORD.copy());
		}
	}

	public void tick() {
		if (this.player.getY() < this.phase.getMinY()) {
			this.player.damage(DamageSource.OUT_OF_WORLD, Integer.MAX_VALUE);
		}

		if (this.respawnCooldown > -1) {
			if (this.respawnCooldown > 0 && this.respawnCooldown % 20 == 0) {
				this.player.sendMessage(new TranslatableText("text.cakewars.respawning", this.respawnCooldown / 20), true);
			} else if (this.respawnCooldown == 0) {
				this.spawn(false);
			}
			this.respawnCooldown -= 1;
		}
	}

	public void eliminate(boolean remove) {
		this.phase.getGameSpace().getPlayers().sendMessage(this.getEliminationMessage());

		if (remove) {
			this.phase.getPlayers().remove(this);
		}
		this.getPlayer().setGameMode(GameMode.SPECTATOR);
	}

	public Text getEliminationMessage() {
		return new TranslatableText("text.cakewars.eliminated", this.player.getDisplayName()).formatted(Formatting.RED);
	}
}
