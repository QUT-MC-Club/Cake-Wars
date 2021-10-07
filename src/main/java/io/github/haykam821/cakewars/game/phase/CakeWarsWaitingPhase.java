package io.github.haykam821.cakewars.game.phase;

import io.github.haykam821.cakewars.game.CakeWarsConfig;
import io.github.haykam821.cakewars.game.map.CakeWarsMap;
import io.github.haykam821.cakewars.game.map.CakeWarsMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;

public class CakeWarsWaitingPhase {
	private final GameSpace gameSpace;
	private final CakeWarsMap map;
	private final TeamSelectionLobby teamSelection;
	private final CakeWarsConfig config;

	public CakeWarsWaitingPhase(GameSpace gameSpace, CakeWarsMap map, TeamSelectionLobby teamSelection, CakeWarsConfig config) {
		this.gameSpace = gameSpace;
		this.map = map;
		this.teamSelection = teamSelection;
		this.config = config;
	}

	public static void setRules(GameLogic game) {
		game.deny(GameRule.BLOCK_DROPS);
		game.deny(GameRule.BREAK_BLOCKS);
		game.deny(GameRule.CRAFTING);
		game.deny(GameRule.FALL_DAMAGE);
		game.deny(GameRule.HUNGER);
		game.deny(GameRule.INTERACTION);
		game.deny(GameRule.PLACE_BLOCKS);
		game.deny(GameRule.PORTALS);
		game.deny(GameRule.PVP);
		game.deny(GameRule.TEAM_CHAT);
		game.deny(GameRule.THROW_ITEMS);
	}

	public static GameOpenProcedure open(GameOpenContext<CakeWarsConfig> context) {
		CakeWarsConfig config = context.getConfig();

		CakeWarsMapBuilder mapBuilder = new CakeWarsMapBuilder(config);
		CakeWarsMap map = mapBuilder.create();

		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
			.setGenerator(map.createGenerator(context.getServer()))
			.setDefaultGameMode(GameMode.ADVENTURE);

		return context.createOpenProcedure(worldConfig, game -> {
			TeamSelectionLobby teamSelection = TeamSelectionLobby.applyTo(game, config.getTeams());
			CakeWarsWaitingPhase phase = new CakeWarsWaitingPhase(game.getGameSpace(), map, teamSelection, config);
			GameWaitingLobby.applyTo(game, config.getPlayerConfig());

			CakeWarsWaitingPhase.setRules(game);

			// Listeners
			game.listen(PlayerAddListener.EVENT, phase::addPlayer);
			game.listen(PlayerDeathListener.EVENT, phase::onPlayerDeath);
			game.listen(OfferPlayerListener.EVENT, phase::offerPlayer);
			game.listen(RequestStartListener.EVENT, phase::requestStart);
		});
	}

	private boolean isFull() {
		return this.gameSpace.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers();
	}

	private JoinResult offerPlayer(ServerPlayerEntity player) {
		return this.isFull() ? JoinResult.gameFull() : JoinResult.ok();
	}

	private StartResult requestStart() {
		PlayerConfig playerConfig = this.config.getPlayerConfig();
		if (this.gameSpace.getPlayerCount() < playerConfig.getMinPlayers()) {
			return StartResult.NOT_ENOUGH_PLAYERS;
		}

		CakeWarsActivePhase.open(this.gameSpace, this.map, this.teamSelection, this.config);
		return StartResult.OK;
	}

	private void addPlayer(ServerPlayerEntity player) {
		CakeWarsActivePhase.spawnAtCenter(this.gameSpace.getWorld(), this.map, player);
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		CakeWarsActivePhase.spawnAtCenter(this.gameSpace.getWorld(), this.map, player);
		return ActionResult.FAIL;
	}
}