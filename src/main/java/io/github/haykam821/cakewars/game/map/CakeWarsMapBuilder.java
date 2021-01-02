package io.github.haykam821.cakewars.game.map;

import java.io.IOException;

import io.github.haykam821.cakewars.game.CakeWarsConfig;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;

public class CakeWarsMapBuilder {
	private final CakeWarsConfig config;

	public CakeWarsMapBuilder(CakeWarsConfig config) {
		this.config = config;
	}

	public CakeWarsMap create() {
		try {
			MapTemplate template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.getMap());
			return new CakeWarsMap(template);
		} catch (IOException exception) {
			throw new GameOpenException(new TranslatableText("text.cakewars.template_load_failed"), exception);
		}
	}
}