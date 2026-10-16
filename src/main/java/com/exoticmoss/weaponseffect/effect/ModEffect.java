package com.exoticmoss.weaponseffect.effect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.exoticmoss.weaponseffect.WeaponsEffect;

public class ModEffect {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS
            = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, WeaponsEffect.MODID);

    /// Darkness Mace zone: the mob walks in zigzags and can't attack.
    public static final DeferredHolder<MobEffect, MobEffect> DISORIENTED = MOB_EFFECTS.register("disoriented", DisorientedEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

}
