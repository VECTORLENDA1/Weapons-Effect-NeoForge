package com.vector.weaponseffect.registry;


import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "weaponseffect");

    public static final Supplier<SimpleParticleType> GUST =
            PARTICLES_TYPES.register("gust", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLES_TYPES.register(eventBus);
    }
}