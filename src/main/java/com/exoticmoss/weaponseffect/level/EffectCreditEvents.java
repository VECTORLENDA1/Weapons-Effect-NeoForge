package com.exoticmoss.weaponseffect.level;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

/// Sends the damage and the deaths of the mobs to EffectCredit (experience from the effects of the abilities).
public class EffectCreditEvents {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) ->
                EffectCredit.onDamage(entity, source, damageTaken));
        ServerLivingEntityEvents.AFTER_DEATH.register(EffectCredit::onDeath);
    }
}
