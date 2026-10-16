package com.exoticmoss.weaponseffect;

import com.exoticmoss.weaponseffect.level.ModDataComponents;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import com.exoticmoss.weaponseffect.effect.ModEffect;
import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModCreativeModTabs;
import com.exoticmoss.weaponseffect.item.ModItems;
import org.slf4j.Logger;

/// Weapons Effect: weapons with effects and abilities, made with the ores of Simple Ores.
/// The weapons are crafted in the Gravity Core of Simple Ores (recipes in data/weaponseffect/recipe/gravity_core/).
@Mod(WeaponsEffect.MODID)
public class WeaponsEffect {
    public static final String MODID = "weaponseffect";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WeaponsEffect(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.Register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModEffect.register(modEventBus);
        ModEntities.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
