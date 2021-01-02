package io.github.haykam821.cakewars.game.map;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;

public class CakeWarsMap {
	private final MapTemplate template;
	private final LongSet initialBlocks = new LongOpenHashSet();

	public CakeWarsMap(MapTemplate template) {
		this.template = template;
		for (BlockPos pos : this.template.getBounds()) {
			if (!template.getBlockState(pos).isAir()) {
				this.initialBlocks.add(pos.asLong());
			}
		}
	}

	public MapTemplate getTemplate() {
		return this.template;
	}

	public boolean isInitialBlock(BlockPos pos) {
		return this.initialBlocks.contains(pos.asLong());
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}