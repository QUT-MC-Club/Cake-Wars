package io.github.haykam821.cakewars.game.player.kit;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Items;

public final class KitTypes {
	public static final List<KitType> KITS = new ArrayList<>();

	public static final KitType BUILDER = register(new KitType(BuilderKit::new, Items.BRICKS, "builder"));
	public static final KitType WARRIOR = register(new KitType(WarriorKit::new, Items.WOODEN_SWORD, "warrior"));
	public static final KitType ARCHER = register(new KitType(ArcherKit::new, Items.BOW, "archer"));
	public static final KitType FROSTING = register(new KitType(FrostingKit::new, FrostingKit.SNOWBALL, "frosting"));

	public static KitType register(KitType type) {
		KITS.add(type);
		return type;
	}
}
