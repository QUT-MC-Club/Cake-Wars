package io.github.haykam821.cakewars.game.tag;

import io.github.haykam821.cakewars.Main;
import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class CakeWarsBlockTags {
	private CakeWarsBlockTags() {
		return;
	}

	public static final TagKey<Block> ARCHER_ARROW_BREAKABLE = CakeWarsBlockTags.of("archer_arrow_breakable");

	private static TagKey<Block> of(String path) {
		Identifier id = new Identifier(Main.MOD_ID, path);
		return TagKey.of(Registry.BLOCK_KEY, id);
	}
}
