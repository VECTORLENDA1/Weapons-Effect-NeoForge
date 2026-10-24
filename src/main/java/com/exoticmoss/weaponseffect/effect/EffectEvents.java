package com.exoticmoss.weaponseffect.effect;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/// What the mod's effects do outside of their own tick.
public class EffectEvents {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !onIncomingDamage(source));
    }

    /// A Disoriented mob can't hurt anyone (melee, arrows, fireballs...).
    /// Returns true when the damage must be cancelled.
    public static boolean onIncomingDamage(DamageSource source) {
        return source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffect.DISORIENTED);
    }
}
