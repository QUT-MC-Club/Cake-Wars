package io.github.haykam821.cakewars.game.shop;

public final class Shops {
	private Shops() {
		return;
	}

	public static final Shop BRICK = new BrickShop();
	public static final Shop EMERALD = new EmeraldShop();
	public static final Shop NETHER_STAR = new NetherStarShop();
}
