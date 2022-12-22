package io.github.haykam821.cakewars.game.player;

import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.player.team.TeamEntry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WinManager {
	private final CakeWarsActivePhase phase;
	private final Object2IntOpenHashMap<TeamEntry> playerCounts = new Object2IntOpenHashMap<>();

	public WinManager(CakeWarsActivePhase phase) {
		this.phase = phase;
		this.playerCounts.defaultReturnValue(0);
	}

	private Text getNoWinnersMessage() {
		return Text.translatable("text.cakewars.no_winners").formatted(Formatting.GOLD);
	}

	private Text getWinningTeamMessage(TeamEntry team) {
		return Text.translatable("text.cakewars.win", team.getName()).formatted(Formatting.GOLD);
	}

	public boolean checkForWinner() {
		this.playerCounts.clear();
		for (PlayerEntry entry : this.phase.getPlayers()) {
			if (entry.getPlayer() != null && entry.getTeam() != null) {
				this.playerCounts.addTo(entry.getTeam(), 1);
			}
		}

		// No teams means no players
		if (this.playerCounts.isEmpty()) {
			this.phase.getGameSpace().getPlayers().sendMessage(this.getNoWinnersMessage());
			return true;
		}

		if (this.phase.isSingleplayer()) {
			return false;
		}

		TeamEntry winningTeam = null;
		for (Object2IntMap.Entry<TeamEntry> entry : this.playerCounts.object2IntEntrySet()) {
			if (entry.getIntValue() > 0) {
				if (winningTeam != null) return false;
				winningTeam = entry.getKey();
			}
		}

		this.phase.getGameSpace().getPlayers().sendMessage(this.getWinningTeamMessage(winningTeam));
		return true;
	}
}
