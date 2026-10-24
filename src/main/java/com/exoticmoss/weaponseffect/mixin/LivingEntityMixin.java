package com.exoticmoss.weaponseffect.mixin;

import com.exoticmoss.weaponseffect.event.EventEffectItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Fabric has no "can this effect be applied" event: the weapon immunities are checked here
/// (canBeAffected is what addEffect and forceAddEffect ask before giving an effect).
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void weaponseffect$weaponImmunities(MobEffectInstance newEffect, CallbackInfoReturnable<Boolean> cir) {
        if (!EventEffectItems.onEffectApplicable((LivingEntity) (Object) this, newEffect)) {
            cir.setReturnValue(false);
        }
    }
}
