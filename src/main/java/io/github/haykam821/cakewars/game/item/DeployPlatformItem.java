package io.github.haykam821.cakewars.game.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.fake.FakeItem;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public class DeployPlatformItem extends Item implements FakeItem {
	private final DyeColor dyeColor;
	private final BlockState placementState;

	public DeployPlatformItem(Item.Settings settings, DyeColor dyeColor) {
		super(settings);
		this.dyeColor = dyeColor;
		this.placementState = ColoredBlocks.wool(dyeColor).getDefaultState();
	}

	public boolean placeAround(BlockPos centerPos, World world) {
		BlockPos minPos = centerPos.add(-1, 0, -1);
		BlockPos maxPos = centerPos.add(1, 0, 1);

		boolean successful = false;
		for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
			BlockState state = world.getBlockState(pos);
			if (state.isAir()) {
				world.setBlockState(pos, this.placementState);
				successful = true;
			}
		}

		return successful;
	}

	public void playSound(World world, PlayerEntity player, BlockPos pos) {
		world.playSound(player, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1, 1);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (!context.getPlayer().abilities.allowModifyWorld) {
			return ActionResult.PASS;
		}

		Direction facing = context.getPlayerFacing();
		BlockPos centerPos = context.getBlockPos().offset(facing, 2);

		if (this.placeAround(centerPos, context.getWorld())) {
			this.playSound(context.getWorld(), context.getPlayer(), centerPos);

			if (!context.getPlayer().abilities.creativeMode) {
				context.getStack().decrement(1);
			}
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	@Override
	public Item asProxy() {
		switch (this.dyeColor) {
			case ORANGE: return Items.ORANGE_DYE;
			case MAGENTA: return Items.MAGENTA_DYE;
			case LIGHT_BLUE: return Items.LIGHT_BLUE_DYE;
			case YELLOW: return Items.YELLOW_DYE;
			case LIME: return Items.LIME_DYE;
			case PINK: return Items.PINK_DYE;
			case GRAY: return Items.GRAY_DYE;
			case LIGHT_GRAY: return Items.LIGHT_GRAY_DYE;
			case CYAN: return Items.CYAN_DYE;
			case PURPLE: return Items.PURPLE_DYE;
			case BLUE: return Items.BLUE_DYE;
			case BROWN: return Items.BROWN_DYE;
			case GREEN: return Items.GREEN_DYE;
			case RED: return Items.RED_DYE;
			case BLACK: return Items.BLACK_DYE;
			default:
			case WHITE: return Items.WHITE_DYE;
		}
	}

	public static Item ofDyeColor(DyeColor dyeColor) {
		switch (dyeColor) {
			case ORANGE: return CakeWarsItems.ORANGE_DEPLOY_PLATFORM.asItem();
			case MAGENTA: return CakeWarsItems.MAGENTA_DEPLOY_PLATFORM.asItem();
			case LIGHT_BLUE: return CakeWarsItems.LIGHT_BLUE_DEPLOY_PLATFORM.asItem();
			case YELLOW: return CakeWarsItems.YELLOW_DEPLOY_PLATFORM.asItem();
			case LIME: return CakeWarsItems.LIME_DEPLOY_PLATFORM.asItem();
			case PINK: return CakeWarsItems.PINK_DEPLOY_PLATFORM.asItem();
			case GRAY: return CakeWarsItems.GRAY_DEPLOY_PLATFORM.asItem();
			case LIGHT_GRAY: return CakeWarsItems.LIGHT_GRAY_DEPLOY_PLATFORM.asItem();
			case CYAN: return CakeWarsItems.CYAN_DEPLOY_PLATFORM.asItem();
			case PURPLE: return CakeWarsItems.PURPLE_DEPLOY_PLATFORM.asItem();
			case BLUE: return CakeWarsItems.BLUE_DEPLOY_PLATFORM.asItem();
			case BROWN: return CakeWarsItems.BROWN_DEPLOY_PLATFORM.asItem();
			case GREEN: return CakeWarsItems.GREEN_DEPLOY_PLATFORM.asItem();
			case RED: return CakeWarsItems.RED_DEPLOY_PLATFORM.asItem();
			case BLACK: return CakeWarsItems.BLACK_DEPLOY_PLATFORM.asItem();
			default:
			case WHITE: return CakeWarsItems.WHITE_DEPLOY_PLATFORM.asItem();
		}
	}
}
