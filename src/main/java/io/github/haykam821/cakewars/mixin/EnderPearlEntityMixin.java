package io.github.haykam821.cakewars.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.github.haykam821.cakewars.Main;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {
	@Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean applyEnderPearlDamageGameRule(Entity entity, DamageSource source, float amount) {
		if (source == DamageSource.FALL) {
			ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(entity.world);
			if (gameSpace != null && gameSpace.testRule(Main.ENDER_PEARL_DAMAGE) == RuleResult.DENY) {
				return false;
			}
		}
		return entity.damage(source, amount);
	}
}
