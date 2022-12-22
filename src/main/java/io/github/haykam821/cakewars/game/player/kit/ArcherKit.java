package io.github.haykam821.cakewars.game.player.kit;

import io.github.haykam821.cakewars.game.player.PlayerEntry;
import io.github.haykam821.cakewars.game.tag.CakeWarsBlockTags;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ArcherKit extends Kit {
	protected static final Item ARROW = Items.ARROW;

	private static final int ARROW_INTERVAL = SharedConstants.TICKS_PER_SECOND * 6;
	private static final int ARROW_RESTOCK_MAX_COUNT = 3;

	public ArcherKit(PlayerEntry player) {
		super(player);
	}

	@Override
	public void tick(int aliveTicks) {
		if (aliveTicks % ARROW_INTERVAL == 0) {
			this.giveIfLessThan(ARROW, ARROW_RESTOCK_MAX_COUNT);
		}
	}

	@Override
	public void onKill() {
		this.give(ARROW);
	}

	@Override
	public boolean canProjectileBreakBlock(PersistentProjectileEntity entity, BlockHitResult hitResult) {
		if (entity.isCritical()) {
			World world = entity.getWorld();
			BlockPos pos = hitResult.getBlockPos();

			BlockState state = world.getBlockState(pos);
			return state.isIn(CakeWarsBlockTags.ARCHER_ARROW_BREAKABLE);
		}

		return false;
	}

	@Override
	public boolean canKeepAfterRespawn(ItemStack stack) {
		return stack.getItem() instanceof RangedWeaponItem;
	}
}
