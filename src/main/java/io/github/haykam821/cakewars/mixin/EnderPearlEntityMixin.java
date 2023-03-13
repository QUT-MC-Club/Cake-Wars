package io.github.haykam821.cakewars.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.github.haykam821.cakewars.Main;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {
	@Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean applyEnderPearlDamageGameRule(Entity entity, DamageSource source, float amount) {
		if (source.isIn(DamageTypeTags.IS_FALL)) {
			ManagedGameSpace gameSpace = GameSpaceManager.get().byWorld(entity.world);
			if (gameSpace != null && gameSpace.getBehavior().testRule(Main.ENDER_PEARL_DAMAGE) == ActionResult.FAIL) {
				return false;
			}
		}
		return entity.damage(source, amount);
	}
}
