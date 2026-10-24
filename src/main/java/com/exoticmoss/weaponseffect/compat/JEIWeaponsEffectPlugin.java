package com.exoticmoss.weaponseffect.compat;


import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import com.exoticmoss.weaponseffect.WeaponsEffect;
import com.exoticmoss.weaponseffect.item.ModItems;

import java.util.LinkedHashMap;
import java.util.Map;


/// JEI integration.
///
/// The weapons are crafted in the Gravity Core of Simple Ores. The recipes
/// (data/weaponseffect/recipe/gravity_core/) are "simpleores:gravity_craft" recipes, so they
/// already show up in the "Gravitational Craft" category that Simple Ores adds to JEI
/// (press R/U on a weapon). This plugin adds an information page to every weapon, saying
/// which Gravity Core can make it and what the weapon does.
@JeiPlugin // (on Fabric the plugin is loaded from the "jei_mod_plugin" entrypoint of fabric.mod.json)
public class JEIWeaponsEffectPlugin implements IModPlugin {
    /// Names of the Gravity Core tiers (translations come from Simple Ores).
    private static final String[] CORE_TIERS = {
            "block.simpleores.gravity_core_sun",
            "block.simpleores.gravity_core_red_giant",
            "block.simpleores.gravity_core_pulsar",
            "block.simpleores.gravity_core_black_hole"
    };

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(WeaponsEffect.MODID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Weapon -> minimum Gravity Core tier of its recipe (keep it equal to the "tier" in the recipe files)
        Map<ItemLike, Integer> weapons = new LinkedHashMap<>();
        weapons.put(ModItems.BLINDNESS_DAGGER, 1);
        weapons.put(ModItems.SWIFTNESS_DAGGER, 1);
        weapons.put(ModItems.POISON_SWORD, 1);
        weapons.put(ModItems.ICE_SWORD, 1);
        weapons.put(ModItems.LANCE, 1);
        weapons.put(ModItems.WITHER_SWORD, 1);
        weapons.put(ModItems.FIRE_SWORD, 1);
        weapons.put(ModItems.GIMLIS_AXE, 2);
        weapons.put(ModItems.DARKNESS_MACE, 2);
        weapons.put(ModItems.HAMMER, 3);
        weapons.put(ModItems.WINGS_OF_DOOM, 4);
        weapons.put(ModItems.SOUL_CORE, 1);

        weapons.forEach((weapon, tier) -> {
            String name = BuiltInRegistries.ITEM.getKey(weapon.asItem()).getPath();
            registration.addIngredientInfo(weapon,
                    Component.translatable("jei.weaponseffect.crafted_in", Component.translatable(CORE_TIERS[tier - 1])),
                    Component.literal(" "),
                    Component.translatable("jei.weaponseffect." + name + ".info"));
        });
    }
}
