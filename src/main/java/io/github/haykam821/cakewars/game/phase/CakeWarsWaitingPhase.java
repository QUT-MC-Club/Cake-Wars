package io.github.haykam821.cakewars.game.phase;

import io.github.haykam821.cakewars.game.CakeWarsConfig;
import io.github.haykam821.cakewars.game.item.CakeWarsItems;
import io.github.haykam821.cakewars.game.item.KitSelectorItem;
import io.github.haykam821.cakewars.game.map.CakeWarsMap;
import io.github.haykam821.cakewars.game.map.CakeWarsMapBuilder;
import io.github.haykam821.cakewars.game.player.kit.selection.KitSelectionManager;
import io.github.haykam821.cakewars.game.player.kit.selection.KitSelectionUi;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class CakeWarsWaitingPhase {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final CakeWarsMap map;
	private final TeamSelectionLobby teamSelection;
	private final KitSelectionManager kitSelection = new KitSelectionManager();;
	private final CakeWarsConfig config;

	public CakeWarsWaitingPhase(GameSpace gameSpace, ServerWorld world, CakeWarsMap map, TeamSelectionLobby teamSelection, CakeWarsConfig config) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.teamSelection = teamSelection;
		this.config = config;
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.BLOCK_DROPS);
		activity.deny(GameRuleType.BREAK_BLOCKS);
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.INTERACTION);
		activity.deny(GameRuleType.PLACE_BLOCKS);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.PVP);
		activity.deny(GameRuleType.THROW_ITEMS);
	}

	public static GameOpenProcedure open(GameOpenContext<CakeWarsConfig> context) {
		CakeWarsConfig config = context.config();

		CakeWarsMapBuilder mapBuilder = new CakeWarsMapBuilder(config);
		CakeWarsMap map = mapBuilder.create(context.server());

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			TeamSelectionLobby teamSelection = TeamSelectionLobby.addTo(activity, config.getTeams());
			CakeWarsWaitingPhase phase = new CakeWarsWaitingPhase(activity.getGameSpace(), world, map, teamSelection, config);
			GameWaitingLobby.addTo(activity, config.getPlayerConfig());

			CakeWarsWaitingPhase.setRules(activity);

			// Listeners
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
			activity.listen(PlayerDamageEvent.EVENT, phase::onPlayerDamage);
			activity.listen(GamePlayerEvents.OFFER, phase::offerPlayer);
			activity.listen(GameActivityEvents.REQUEST_START, phase::requestStart);
			activity.listen(GameActivityEvents.TICK, phase::tick);
			activity.listen(ItemUseEvent.EVENT, phase::useItem);
		});
	}

	private GameResult requestStart() {
		CakeWarsActivePhase.open(this.gameSpace, this.world, this.map, this.teamSelection, this.kitSelection, this.config);
		return GameResult.ok();
	}

	private void tick() {
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			if (!this.map.getBox().contains(player.getPos())) {
				CakeWarsActivePhase.spawnAtCenter(this.world, this.map, player);
			}
		}
	}

	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getSpawnPos()).and(() -> {
			offer.player().changeGameMode(GameMode.ADVENTURE);
			this.giveKitSelector(offer.player());
		});
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		CakeWarsActivePhase.spawnAtCenter(this.world, this.map, player);
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
	}

	private TypedActionResult<ItemStack> useItem(ServerPlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);

		if (stack.getItem() instanceof KitSelectorItem) {
			KitSelectionUi.build(this.kitSelection, player).open();
			return TypedActionResult.success(stack);
		}

		return TypedActionResult.pass(stack);
	}

	private void giveKitSelector(ServerPlayerEntity player) {
		player.getInventory().setStack(8, new ItemStack(CakeWarsItems.KIT_SELECTOR));

		player.currentScreenHandler.sendContentUpdates();
		player.playerScreenHandler.onContentChanged(player.getInventory());
	}
}