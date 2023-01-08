package io.github.haykam821.cakewars.game.player.team;

import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;

public class CakeWarsSidebar {
	private final SidebarWidget widget;
	private final CakeWarsActivePhase phase;
	private final Object2IntOpenHashMap<TeamEntry> playerCounts = new Object2IntOpenHashMap<>();

	public CakeWarsSidebar(GlobalWidgets widgets, CakeWarsActivePhase phase) {
		Text name = Text.translatable("gameType.cakewars.cake_wars").styled(style -> {
			return style.withBold(true);
		});
		this.widget = widgets.addSidebar(name);

		this.phase = phase;
	}

	public void update() {
		this.playerCounts.clear();
		for (PlayerEntry player : this.phase.getPlayers()) {
			if (player.getTeam() != null) {
				this.playerCounts.addTo(player.getTeam(), 1);
			}
		}

		this.widget.set(content -> {
			for (TeamEntry team : this.phase.getTeams()) {
				content.add(team.getSidebarEntry(this.playerCounts.getInt(team)));
			}
		});
	}
}
