package com.vector.weaponseffect.effect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.vector.weaponseffect.WeaponsEffect;

public class ModEffect {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS
            = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, WeaponsEffect.MODID);



    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

}
