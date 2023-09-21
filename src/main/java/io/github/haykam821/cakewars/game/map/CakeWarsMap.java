package io.github.haykam821.cakewars.game.map;

import java.util.function.Predicate;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

public class CakeWarsMap {
	private final MapTemplate template;
	private final LongSet protectedBlocks = new LongOpenHashSet();
	private final Vec3d spawnPos;
	private final Box box;

	public CakeWarsMap(MapTemplate template) {
		this.template = template;

		this.addProtection(this.template.getBounds(), pos -> {
			return !template.getBlockState(pos).isAir();
		});

		this.spawnPos = this.calculateSpawnPos();
		this.box = this.template.getBounds().asBox();
	}

	public MapTemplate getTemplate() {
		return this.template;
	}

	public boolean isProtected(BlockPos pos) {
		return this.protectedBlocks.contains(pos.asLong());
	}

	public void addProtection(BlockPos pos) {
		this.protectedBlocks.add(pos.asLong());
	}

	public void addProtection(BlockBounds bounds, Predicate<BlockPos> predicate) {
		for (BlockPos pos : bounds) {
			if (predicate.test(pos)) {
				this.addProtection(pos);
			}
		}
	}

	public void removeProtection(BlockPos pos) {
		this.protectedBlocks.remove(pos.asLong());
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

	public Box getBox() {
		return this.box;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}