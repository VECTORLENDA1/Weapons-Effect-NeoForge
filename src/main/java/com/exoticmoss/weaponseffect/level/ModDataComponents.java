package com.exoticmoss.weaponseffect.level;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.exoticmoss.weaponseffect.WeaponsEffect.MODID;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    /// Level and ability experience of a weapon
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponLevel>> WEAPON_LEVEL =
            DATA_COMPONENTS.registerComponentType("weapon_level", builder -> builder
                    .persistent(WeaponLevel.CODEC)
                    .networkSynchronized(WeaponLevel.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
