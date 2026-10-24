package com.exoticmoss.weaponseffect.level;

import com.exoticmoss.weaponseffect.WeaponsEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/// Sends the damage and the deaths of the mobs to EffectCredit (experience from the effects of the abilities).
@EventBusSubscriber(modid = WeaponsEffect.MODID)
public class EffectCreditEvents {

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) EffectCredit.onDamage(event.getEntity(), event.getSource(), event.getHealthDamage());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide()) EffectCredit.onDeath(event.getEntity(), event.getSource());
    }
}
