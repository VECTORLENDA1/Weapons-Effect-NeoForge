package com.exoticmoss.weaponseffect.level;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import static com.exoticmoss.weaponseffect.WeaponsEffect.MODID;

public class ModDataComponents {
    /// Level and ability experience of a weapon
    public static final DataComponentType<WeaponLevel> WEAPON_LEVEL = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(MODID, "weapon_level"),
            DataComponentType.<WeaponLevel>builder()
                    .persistent(WeaponLevel.CODEC)
                    .networkSynchronized(WeaponLevel.STREAM_CODEC)
                    .build());

    /// Loads the class, so the component above is registered.
    public static void register() {
    }
}
