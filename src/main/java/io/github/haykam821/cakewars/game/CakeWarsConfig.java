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
			Codec.INT.optionalFieldOf("respawn_cooldown", 20 * 5).forGetter(CakeWarsConfig::getRespawnCooldown),
			Codec.INT.optionalFieldOf("cake_eat_cooldown", 12).forGetter(CakeWarsConfig::getCakeEatCooldown),
			Codec.INT.optionalFieldOf("brick_generator_cooldown", 50).forGetter(CakeWarsConfig::getBrickGeneratorCooldown),
			Codec.INT.optionalFieldOf("emerald_generator_cooldown", 150).forGetter(CakeWarsConfig::getEmeraldGeneratorCooldown),
			Codec.INT.optionalFieldOf("nether_star_generator_cooldown", 300).forGetter(CakeWarsConfig::getNetherStarGeneratorCooldown),
			Codec.INT.optionalFieldOf("max_beacon_health", 20 * 8).forGetter(CakeWarsConfig::getMaxBeaconHealth),
			Codec.BOOL.optionalFieldOf("allow_self_eating", false).forGetter(CakeWarsConfig::shouldAllowSelfEating)
		).apply(instance, CakeWarsConfig::new);
	});

	private final Identifier map;
	private final PlayerConfig playerConfig;
	private final List<GameTeam> teams;
	private final int respawnCooldown;
	private final int cakeEatCooldown;
	private final int brickGeneratorCooldown;
	private final int emeraldGeneratorCooldown;
	private final int netherStarGeneratorCooldown;
	private final int maxBeaconHealth;
	private final boolean allowSelfEating;

	public CakeWarsConfig(Identifier map, PlayerConfig playerConfig, List<GameTeam> teams, int respawnCooldown, int cakeEatCooldown, int brickGeneratorCooldown, int emeraldGeneratorCooldown, int netherStarGeneratorCooldown, int maxBeaconHealth, boolean allowSelfEating) {
		this.map = map;
		this.playerConfig = playerConfig;
		this.teams = teams;
		this.respawnCooldown = respawnCooldown;
		this.cakeEatCooldown = cakeEatCooldown;
		this.brickGeneratorCooldown = brickGeneratorCooldown;
		this.emeraldGeneratorCooldown = emeraldGeneratorCooldown;
		this.netherStarGeneratorCooldown = netherStarGeneratorCooldown;
		this.maxBeaconHealth = maxBeaconHealth;
		this.allowSelfEating = allowSelfEating;
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

	public int getRespawnCooldown() {
		return this.respawnCooldown;
	}

	public int getCakeEatCooldown() {
		return this.cakeEatCooldown;
	}

	public int getBrickGeneratorCooldown() {
		return this.brickGeneratorCooldown;
	}

	public int getEmeraldGeneratorCooldown() {
		return this.emeraldGeneratorCooldown;
	}

	public int getNetherStarGeneratorCooldown() {
		return this.netherStarGeneratorCooldown;
	}

	public int getMaxBeaconHealth() {
		return this.maxBeaconHealth;
	}

	public boolean shouldAllowSelfEating() {
		return this.allowSelfEating;
	}
}