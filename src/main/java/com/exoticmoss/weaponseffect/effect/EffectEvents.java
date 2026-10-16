package com.exoticmoss.weaponseffect.effect;

import com.exoticmoss.weaponseffect.WeaponsEffect;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/// What the mod's effects do outside of their own tick.
@EventBusSubscriber(modid = WeaponsEffect.MODID)
public class EffectEvents {

    /// A Disoriented mob can't hurt anyone (melee, arrows, fireballs...).
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffect.DISORIENTED)) {
            event.setCanceled(true);
        }
    }
}
