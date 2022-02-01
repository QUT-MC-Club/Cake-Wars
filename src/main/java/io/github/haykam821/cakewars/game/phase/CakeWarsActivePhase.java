package io.github.haykam821.cakewars.game.phase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.github.haykam821.cakewars.Main;
import io.github.haykam821.cakewars.game.CakeWarsConfig;
import io.github.haykam821.cakewars.game.event.ThrowEnderPearlListener;
import io.github.haykam821.cakewars.game.event.UseEntityListener;
import io.github.haykam821.cakewars.game.map.CakeWarsMap;
import io.github.haykam821.cakewars.game.player.Beacon;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import io.github.haykam821.cakewars.game.player.WinManager;
import io.github.haykam821.cakewars.game.player.team.CakeWarsSidebar;
import io.github.haykam821.cakewars.game.player.team.TeamEntry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamChat;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class CakeWarsActivePhase implements BlockBreakEvent, GameActivityEvents.Enable, GameActivityEvents.Tick, BlockPlaceEvent.Before, GamePlayerEvents.Offer, PlayerDeathEvent, GamePlayerEvents.Remove, ThrowEnderPearlListener, BlockUseEvent, UseEntityListener {
	private final ServerWorld world;
	private final GameSpace gameSpace;
	private final CakeWarsMap map;
	private final CakeWarsConfig config;
	private final Set<PlayerEntry> players;
	private final Map<GameTeamKey, TeamEntry> teams = new HashMap<>();;
	private final TeamManager teamManager;
	private final Set<Beacon> beacons = new HashSet<>();
	private final WinManager winManager = new WinManager(this);
	private final CakeWarsSidebar sidebar;
	private boolean singleplayer;

	public CakeWarsActivePhase(GameSpace gameSpace, ServerWorld world, CakeWarsMap map, TeamManager teamManager, GlobalWidgets widgets, CakeWarsConfig config) {
		this.world = world;
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;

		this.players = new HashSet<>(this.gameSpace.getPlayers().size());
		this.teamManager = teamManager;
		
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			GameTeamKey teamKey = this.teamManager.teamFor(player);

			TeamEntry teamEntry = this.teams.get(teamKey);
			if (teamEntry == null) {
				teamEntry = new TeamEntry(this, this.teamManager.getTeamConfig(teamKey), this.map.getTemplate(), teamKey);
				this.teams.put(teamKey, teamEntry);
			}

			this.players.add(new PlayerEntry(this, player, teamEntry));
		}

		this.sidebar = new CakeWarsSidebar(widgets, this);
	}

	public static void setRules(GameActivity activity) {
		activity.allow(GameRuleType.BLOCK_DROPS);
		activity.allow(GameRuleType.BREAK_BLOCKS);
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(Main.ENDER_PEARL_DAMAGE);
		activity.allow(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.allow(GameRuleType.INTERACTION);
		activity.deny(GameRuleType.MODIFY_ARMOR);
		activity.allow(GameRuleType.PLACE_BLOCKS);
		activity.deny(GameRuleType.PORTALS);
		activity.allow(GameRuleType.PVP);
		activity.allow(GameRuleType.THROW_ITEMS);
	}

	public static void open(GameSpace gameSpace, ServerWorld world, CakeWarsMap map, TeamSelectionLobby teamSelection, CakeWarsConfig config) {
		gameSpace.setActivity(activity -> {
			TeamManager teamManager = TeamManager.addTo(activity);
			TeamChat.addTo(activity, teamManager);

			for (GameTeam team : config.getTeams()) {
				GameTeamConfig teamConfig = GameTeamConfig.builder(team.config())
					.setFriendlyFire(false)
					.setCollision(Team.CollisionRule.PUSH_OTHER_TEAMS)
					.build();

				teamManager.addTeam(team.key(), teamConfig);
			}

			teamSelection.allocate(gameSpace.getPlayers(), (teamKey, player) -> {
				teamManager.addPlayerTo(player, teamKey);
			});

			GlobalWidgets widgets = GlobalWidgets.addTo(activity);
			CakeWarsActivePhase phase = new CakeWarsActivePhase(gameSpace, world, map, teamManager, widgets, config);

			CakeWarsActivePhase.setRules(activity);

			// Listeners
			activity.listen(BlockBreakEvent.EVENT, phase);
			activity.listen(GameActivityEvents.ENABLE, phase);
			activity.listen(GameActivityEvents.TICK, phase);
			activity.listen(BlockPlaceEvent.BEFORE, phase);
			activity.listen(GamePlayerEvents.OFFER, phase);
			activity.listen(PlayerDeathEvent.EVENT, phase);
			activity.listen(GamePlayerEvents.REMOVE, phase);
			activity.listen(ThrowEnderPearlListener.EVENT, phase);
			activity.listen(BlockUseEvent.EVENT, phase);
			activity.listen(UseEntityListener.EVENT, phase);
		});
	}

	// Listeners
	@Override
	public ActionResult onBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
		if (this.map.isInitialBlock(pos)) {
			return ActionResult.FAIL;
		}
		return ActionResult.PASS;
	}

	@Override
	public void onEnable() {
		this.singleplayer = this.players.size() == 1;

		for (PlayerEntry player : this.players) {
			player.spawn(false);
		}

		MapTemplateMetadata metadata = this.map.getTemplate().getMetadata();
		metadata.getRegions("brick_villager").forEach(this::spawnShopVillager);
		metadata.getRegions("emerald_villager").forEach(this::spawnShopVillager);
		metadata.getRegions("nether_star_villager").forEach(this::spawnShopVillager);


		metadata.getRegions("emerald_beacon").forEach(region -> {
			this.beacons.add(new Beacon(this, region, Items.EMERALD, this.config.getEmeraldGeneratorCooldown()));
		});
		metadata.getRegions("nether_star_beacon").forEach(region -> {
			this.beacons.add(new Beacon(this, region, Items.NETHER_STAR, this.config.getNetherStarGeneratorCooldown()));
		});

		this.sidebar.update();
	}

	@Override
	public void onTick() {
		Iterator<PlayerEntry> playerIterator = this.players.iterator();
		boolean updateSidebar = false;
		while (playerIterator.hasNext()) {
			PlayerEntry player = playerIterator.next();
			if (player.tick()) {
				playerIterator.remove();
				updateSidebar = true;
			}
		}

		if (updateSidebar) {
			this.sidebar.update();
		}

		for (TeamEntry team : this.getTeams()) {
			team.tick();
		}
		for (Beacon beacon : this.beacons) {
			beacon.tick();
		}

		// Attempt to determine a winner
		if (this.winManager.checkForWinner()) {
			gameSpace.close(GameCloseReason.FINISHED);
		}
	}

	@Override
	public ActionResult onPlace(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemUsageContext context) {
		if (this.map.isInitialBlock(pos)) {
			return ActionResult.FAIL;
		}
		return ActionResult.PASS;
	}

	@Override
	public PlayerOfferResult onOfferPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getSpawnPos()).and(() -> {
			this.setSpectator(offer.player());
		});
	}

	@Override
	public ActionResult onDeath(ServerPlayerEntity player, DamageSource source) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry == null) {
			CakeWarsActivePhase.spawnAtCenter(world, map, player);
			return ActionResult.FAIL;
		} else {
			return entry.onDeath(source);
		}
	}


	@Override
	public void onRemovePlayer(ServerPlayerEntity player) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry != null) {
			entry.eliminate(true);
		}
	}

	@Override
	public int onThrowEnderPearl(World world, ServerPlayerEntity user, Hand hand) {
		return this.config.getEnderPearlCooldown();
	}

	@Override
	public ActionResult onUse(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry != null) {
			return entry.onUseBlock(hand, hitResult);
		}

		return ActionResult.FAIL;
	}

	@Override
	public ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
		PlayerEntry entry = this.getPlayerEntry((ServerPlayerEntity) player);
		if (entry != null) {
			return entry.onUseEntity(player, entity);
		}
		return ActionResult.FAIL;
	}

	// Getters
	public GameSpace getGameSpace() {
		return this.gameSpace;
	}

	public ServerWorld getWorld() {
		return this.world;
	}

	public CakeWarsMap getMap() {
		return this.map;
	}

	public int getMinY() {
		return this.map.getTemplate().getBounds().min().getY() - this.config.getOutOfBoundsBuffer();
	}

	public CakeWarsConfig getConfig() {
		return this.config;
	}

	public Set<PlayerEntry> getPlayers() {
		return this.players;
	}

	public Iterable<TeamEntry> getTeams() {
		return this.teams.values();
	}

	public CakeWarsSidebar getSidebar() {
		return this.sidebar;
	}

	public boolean isSingleplayer() {
		return this.singleplayer;
	}

	// Utilities
	private void spawnShopVillager(TemplateRegion region) {
		VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, this.world);
			
		Vec3d centerPos = region.getBounds().center();
		float yaw = region.getData().getFloat("Rotation");
		villager.refreshPositionAndAngles(centerPos.getX(), region.getBounds().min().getY(), centerPos.getZ(), yaw, 0);

		villager.setAiDisabled(true);
		villager.setInvulnerable(true);
		villager.setNoGravity(true);
		villager.setSilent(true);

		this.world.getChunk(villager.getBlockPos());
		this.world.spawnEntity(villager);
		villager.refreshPositionAndAngles(villager.getPos().getX(), villager.getPos().getY(), villager.getPos().getZ(), yaw, 0);
	}

	public void pling() {
		this.getGameSpace().getPlayers().playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1, 1);
	}

	public PlayerEntry getPlayerEntry(ServerPlayerEntity player) {
		for (PlayerEntry entry : this.players) {
			if (player == entry.getPlayer()) {
				return entry;
			}
		}
		return null;
	}

	private void setSpectator(ServerPlayerEntity player) {
		player.changeGameMode(GameMode.SPECTATOR);
	}

	public static void spawnAtCenter(ServerWorld world, CakeWarsMap map, ServerPlayerEntity player) {
		Vec3d spawnPos = map.getSpawnPos(); 
		player.teleport(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
	}
}