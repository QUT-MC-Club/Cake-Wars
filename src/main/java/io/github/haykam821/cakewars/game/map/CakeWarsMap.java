package io.github.haykam821.cakewars.game.map;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

public class CakeWarsMap {
	private final MapTemplate template;
	private final LongSet initialBlocks = new LongOpenHashSet();
	private final Vec3d spawnPos;

	public CakeWarsMap(MapTemplate template) {
		this.template = template;
		for (BlockPos pos : this.template.getBounds()) {
			if (!template.getBlockState(pos).isAir()) {
				this.initialBlocks.add(pos.asLong());
			}
		}

		this.spawnPos = this.calculateSpawnPos();
	}

	public MapTemplate getTemplate() {
		return this.template;
	}

	public boolean isInitialBlock(BlockPos pos) {
		return this.initialBlocks.contains(pos.asLong());
	}

	public void removeInitialBlock(BlockPos pos) {
		this.initialBlocks.remove(pos.asLong());
	}

	public Vec3d getSpawnPos() {
		return this.spawnPos;
	}

	private Vec3d calculateSpawnPos() {
		TemplateRegion spawnRegion = this.template.getMetadata().getFirstRegion("nether_star_beacon");
		if (spawnRegion == null) {
			return this.template.getBounds().center();
		}

		Vec3d center = spawnRegion.getBounds().center();
		return new Vec3d(center.getX(), spawnRegion.getBounds().min().getY() + 2, center.getZ());
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}