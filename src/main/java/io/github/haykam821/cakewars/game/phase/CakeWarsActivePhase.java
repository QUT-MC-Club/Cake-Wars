package io.github.haykam821.cakewars.game.phase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.haykam821.cakewars.game.CakeWarsConfig;
import io.github.haykam821.cakewars.game.event.UseEntityListener;
import io.github.haykam821.cakewars.game.map.CakeWarsMap;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import io.github.haykam821.cakewars.game.player.TeamEntry;
import io.github.haykam821.cakewars.game.player.WinManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
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
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.BreakBlockListener;
import xyz.nucleoid.plasmid.game.event.GameCloseListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlaceBlockListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public class CakeWarsActivePhase implements BreakBlockListener, GameCloseListener, GameOpenListener, GameTickListener, UseEntityListener, PlaceBlockListener, PlayerAddListener, PlayerDeathListener, UseBlockListener {
	private final ServerWorld world;
	private final GameSpace gameSpace;
	private final CakeWarsMap map;
	private final CakeWarsConfig config;
	private final Set<PlayerEntry> players;
	private final Set<TeamEntry> teams;
	private final WinManager winManager = new WinManager(this);
	private boolean singleplayer;
	private boolean opened;

	public CakeWarsActivePhase(GameSpace gameSpace, CakeWarsMap map, TeamSelectionLobby teamSelection, CakeWarsConfig config) {
		this.world = gameSpace.getWorld();
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;

		this.players = new HashSet<>(this.gameSpace.getPlayerCount());
		this.teams = new HashSet<>(this.config.getTeams().size());
		Map<GameTeam, TeamEntry> gameTeamsToEntries = new HashMap<>(this.config.getTeams().size());

		MinecraftServer server = this.world.getServer();
		ServerScoreboard scoreboard = server.getScoreboard();

		teamSelection.allocate((gameTeam, player) -> {
			// Get or create team
			TeamEntry team = gameTeamsToEntries.get(gameTeam);
			if (team == null) {
				team = new TeamEntry(this, gameTeam, server, this.map.getTemplate());
				this.teams.add(team);
				gameTeamsToEntries.put(gameTeam, team);
			}

			this.players.add(new PlayerEntry(this, player, team));
			scoreboard.addPlayerToTeam(player.getEntityName(), team.getScoreboardTeam());
		});
	}

	public static void setRules(GameLogic game) {
		game.setRule(GameRule.BLOCK_DROPS, RuleResult.ALLOW);
		game.setRule(GameRule.BREAK_BLOCKS, RuleResult.ALLOW);
		game.setRule(GameRule.CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.ALLOW);
		game.setRule(GameRule.HUNGER, RuleResult.DENY);
		game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);
		game.setRule(GameRule.PLACE_BLOCKS, RuleResult.ALLOW);
		game.setRule(GameRule.PORTALS, RuleResult.DENY);
		game.setRule(GameRule.PVP, RuleResult.ALLOW);
		game.setRule(GameRule.TEAM_CHAT, RuleResult.ALLOW);
		game.setRule(GameRule.THROW_ITEMS, RuleResult.ALLOW);
	}

	public static void open(GameSpace gameSpace, CakeWarsMap map, TeamSelectionLobby teamSelection, CakeWarsConfig config) {
		CakeWarsActivePhase phase = new CakeWarsActivePhase(gameSpace, map, teamSelection, config);

		gameSpace.openGame(game -> {
			CakeWarsActivePhase.setRules(game);

			// Listeners
			game.on(BreakBlockListener.EVENT, phase);
			game.on(GameOpenListener.EVENT, phase);
			game.on(GameTickListener.EVENT, phase);
			game.on(PlaceBlockListener.EVENT, phase);
			game.on(PlayerAddListener.EVENT, phase);
			game.on(PlayerDeathListener.EVENT, phase);
			game.on(UseBlockListener.EVENT, phase);
			game.on(UseEntityListener.EVENT, phase);
		});
	}

	// Listeners
	@Override
	public ActionResult onBreak(ServerPlayerEntity player, BlockPos pos) {
		if (this.map.isInitialBlock(pos)) {
			return ActionResult.FAIL;
		}
		return ActionResult.PASS;
	}

	@Override
	public void onClose() {
		MinecraftServer server = this.world.getServer();
		ServerScoreboard scoreboard = server.getScoreboard();

		for (TeamEntry team : this.teams) {
			scoreboard.removeTeam(team.getScoreboardTeam());
		}
	}

	@Override
	public void onOpen() {
		this.opened = true;
		this.singleplayer = this.players.size() == 1;

		for (PlayerEntry player : this.players) {
			player.spawn();
		}

		this.map.getTemplate().getMetadata().getRegions("brick_villager").forEach(region -> {
			VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, this.world);
			
			Vec3d centerPos = region.getBounds().getCenter();
			float yaw = region.getData().getFloat("Rotation");
			villager.refreshPositionAndAngles(centerPos.getX(), region.getBounds().getMin().getY(), centerPos.getZ(), yaw, 0);

			villager.setAiDisabled(true);
			villager.setInvulnerable(true);
			villager.setNoGravity(true);

			this.world.getChunk(villager.getBlockPos());
			this.world.spawnEntity(villager);
			villager.refreshPositionAndAngles(villager.getPos().getX(), villager.getPos().getY(), villager.getPos().getZ(), yaw, 0);
		});
	}

	@Override
	public void onTick() {
		for (PlayerEntry player : this.players) {
			player.tick();
		}
		for (TeamEntry team : this.teams) {
			team.tick();
		}

		// Attempt to determine a winner
		if (this.winManager.checkForWinner()) {
			gameSpace.close(GameCloseReason.FINISHED);
		}
	}

	@Override
	public ActionResult onPlace(ServerPlayerEntity player, BlockPos pos, BlockState state, ItemUsageContext context) {
		if (this.map.isInitialBlock(pos)) {
			return ActionResult.FAIL;
		}
		return ActionResult.PASS;
	}

	@Override
	public void onAddPlayer(ServerPlayerEntity player) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry == null) {
			this.setSpectator(player);
		} else if (this.opened) {
			entry.eliminate(true);
		}
	}

	@Override
	public ActionResult onDeath(ServerPlayerEntity player, DamageSource source) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry == null) {
			CakeWarsActivePhase.spawn(world, map, player);
			return ActionResult.PASS;
		} else {
			return entry.onDeath(player, source);
		}
	}

	@Override
	public ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry != null) {
			return entry.onUseBlock(player, hand, hitResult);
		}

		return ActionResult.PASS;
	}

	@Override
	public ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
		PlayerEntry entry = this.getPlayerEntry((ServerPlayerEntity) player);
		if (entry != null) {
			return entry.onUseEntity(player, world, hand, entity, hitResult);
		}
		return ActionResult.PASS;
	}

	// Getters
	public GameSpace getGameSpace() {
		return this.gameSpace;
	}

	public CakeWarsMap getMap() {
		return this.map;
	}

	public int getMinY() {
		return this.map.getTemplate().getBounds().getMin().getY();
	}

	public CakeWarsConfig getConfig() {
		return this.config;
	}

	public Set<PlayerEntry> getPlayers() {
		return this.players;
	}

	public Set<TeamEntry> getTeams() {
		return this.teams;
	}

	public boolean isSingleplayer() {
		return this.singleplayer;
	}

	// Utilities
	public void pling() {
		this.getGameSpace().getPlayers().sendSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1, 1);
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
		player.setGameMode(GameMode.SPECTATOR);
	}

	public static void spawn(ServerWorld world, CakeWarsMap map, ServerPlayerEntity player) {
		player.teleport(world, 0, 128, 0, 0, 0);
	}
}