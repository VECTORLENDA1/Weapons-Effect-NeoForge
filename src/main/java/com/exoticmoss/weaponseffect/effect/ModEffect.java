package com.exoticmoss.weaponseffect.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import com.exoticmoss.weaponseffect.WeaponsEffect;

public class ModEffect {
    /// Darkness Mace zone: the mob walks in zigzags and can't attack.
    public static final Holder<MobEffect> DISORIENTED = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(WeaponsEffect.MODID, "disoriented"), new DisorientedEffect());

    /// Loads the class, so the effect above is registered.
    public static void register() {
    }

}
