package io.github.haykam821.cakewars.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.github.haykam821.cakewars.game.item.FrostingSnowballItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(SnowballEntity.class)
public abstract class SnowballEntityMixin extends ThrownItemEntity {
	public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> type, World world) {
		super(type, world);
	}

	@Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean applySlowness(Entity entity, DamageSource source, float amount) {
		if (entity.damage(source, amount)) {
			ItemStack stack = this.getStack();

			if (stack.getItem() instanceof FrostingSnowballItem) {
				FrostingSnowballItem.applySlowness(entity);
			}

			return true;
		}

		return false;
	}
}
