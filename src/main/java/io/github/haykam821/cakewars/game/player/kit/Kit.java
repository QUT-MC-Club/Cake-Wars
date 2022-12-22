package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;

public abstract class Kit {
	protected PlayerEntry player;

	public Kit(PlayerEntry player) {
		this.player = player;
	}

	public void tick(int aliveTicks) {
		return;
	}

	public void onKill() {
		return;
	}

	public boolean canProjectileBreakBlock(PersistentProjectileEntity entity, BlockHitResult hitResult) {
		return false;
	}

	public boolean canKeepAfterRespawn(ItemStack stack) {
		return false;
	}

	protected void give(ItemConvertible item) {
		this.player.getPlayer().giveItemStack(new ItemStack(item));
	}

	protected void giveIfLessThan(ItemConvertible item, int maxCount) {
		if (this.player.hasLessThan(item, maxCount)) {
			this.give(item);
		}
	}
}
