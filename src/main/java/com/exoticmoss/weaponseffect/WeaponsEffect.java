package com.exoticmoss.weaponseffect;

import com.exoticmoss.weaponseffect.effect.EffectEvents;
import com.exoticmoss.weaponseffect.event.EventEffectItems;
import com.exoticmoss.weaponseffect.level.ModDataComponents;
import com.exoticmoss.weaponseffect.level.WeaponLevelCommand;
import com.exoticmoss.weaponseffect.ritual.RitualEvents;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import com.exoticmoss.weaponseffect.effect.ModEffect;
import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModCreativeModTabs;
import com.exoticmoss.weaponseffect.item.ModItems;
import org.slf4j.Logger;

/// Weapons Effect: weapons with effects and abilities, made with the ores of Simple Ores.
/// The weapons are crafted in the Gravity Core of Simple Ores (recipes in data/weaponseffect/recipe/gravity_core/).
public class WeaponsEffect implements ModInitializer {
    public static final String MODID = "weaponseffect";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ModItems.Register();
        ModDataComponents.register();
        ModEffect.register();
        ModEntities.register();
        ModCreativeModTabs.register();

        EventEffectItems.register();
        EffectEvents.register();
        com.exoticmoss.weaponseffect.level.EffectCreditEvents.register();
        WeaponLevelCommand.register();
        RitualEvents.register();
    }
}
