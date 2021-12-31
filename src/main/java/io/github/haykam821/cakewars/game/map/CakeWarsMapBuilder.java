package io.github.haykam821.cakewars.game.map;

import java.io.IOException;

import io.github.haykam821.cakewars.game.CakeWarsConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.GameOpenException;

public class CakeWarsMapBuilder {
	private final CakeWarsConfig config;

	public CakeWarsMapBuilder(CakeWarsConfig config) {
		this.config = config;
	}

	public CakeWarsMap create(MinecraftServer server) {
		try {
			MapTemplate template = MapTemplateSerializer.loadFromResource(server, this.config.getMap());
			return new CakeWarsMap(template);
		} catch (IOException exception) {
			throw new GameOpenException(new TranslatableText("text.cakewars.template_load_failed"), exception);
		}
	}
}