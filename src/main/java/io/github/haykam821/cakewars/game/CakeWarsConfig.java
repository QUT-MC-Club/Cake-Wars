package io.github.haykam821.cakewars.game;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.player.GameTeam;

public class CakeWarsConfig {
	public static final Codec<CakeWarsConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Identifier.CODEC.fieldOf("map").forGetter(CakeWarsConfig::getMap),
			PlayerConfig.CODEC.fieldOf("players").forGetter(CakeWarsConfig::getPlayerConfig),
			GameTeam.CODEC.listOf().fieldOf("teams").forGetter(CakeWarsConfig::getTeams),
			Codec.INT.optionalFieldOf("cake_eat_cooldown", 12).forGetter(CakeWarsConfig::getCakeEatCooldown),
			Codec.INT.optionalFieldOf("generator_cooldown", 50).forGetter(CakeWarsConfig::getGeneratorCooldown)
		).apply(instance, CakeWarsConfig::new);
	});

	private final Identifier map;
	private final PlayerConfig playerConfig;
	private final List<GameTeam> teams;
	private final int cakeEatCooldown;
	private final int generatorCooldown;

	public CakeWarsConfig(Identifier map, PlayerConfig playerConfig, List<GameTeam> teams, int cakeEatCooldown, int generatorCooldown) {
		this.map = map;
		this.playerConfig = playerConfig;
		this.teams = teams;
		this.cakeEatCooldown = cakeEatCooldown;
		this.generatorCooldown = generatorCooldown;
	}

	public Identifier getMap() {
		return this.map;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public List<GameTeam> getTeams() {
		return this.teams;
	}

	public int getCakeEatCooldown() {
		return this.cakeEatCooldown;
	}

	public int getGeneratorCooldown() {
		return this.generatorCooldown;
	}
}