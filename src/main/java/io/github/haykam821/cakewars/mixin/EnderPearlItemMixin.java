package io.github.haykam821.cakewars.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import io.github.haykam821.cakewars.game.event.ThrowEnderPearlListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;

@Mixin(EnderPearlItem.class)
public class EnderPearlItemMixin {
	@ModifyConstant(method = "use", constant = @Constant(intValue = 20))
	private int modifyCooldown(int cooldown, World world, PlayerEntity user, Hand hand) {
		if (world.isClient) return cooldown;

		ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
		if (gameSpace == null) return cooldown;
		if (!gameSpace.containsEntity(user)) return cooldown;
		
		int newCooldown = gameSpace.invoker(ThrowEnderPearlListener.EVENT).onThrowEnderPearl(world, (ServerPlayerEntity) user, hand);
		return newCooldown < 0 ? cooldown : newCooldown;
	}
}
