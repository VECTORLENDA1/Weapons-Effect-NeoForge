package com.exoticmoss.weaponseffect.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import static com.exoticmoss.weaponseffect.WeaponsEffect.MODID;


//Creates the mod tab in the creative inventory, with all the weapons
public class ModCreativeModTabs {
    public static final CreativeModeTab WEAPONS_EFFECT = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(MODID, "weapons_effect"),
            FabricCreativeModeTab.builder()
            .title(Component.translatable("creativetab.weapons_effect"))
            .icon(() -> ModItems.WINGS_OF_DOOM.getDefaultInstance())
            .displayItems((parameters, output) -> {


                    //WEAPONS//
                    output.accept(ModItems.FIRE_SWORD);
                    output.accept(ModItems.WITHER_SWORD);
                    output.accept(ModItems.BLINDNESS_DAGGER);
                    output.accept(ModItems.DARKNESS_MACE);
                    output.accept(ModItems.GIMLIS_AXE);
                    output.accept(ModItems.ICE_SWORD);
                    output.accept(ModItems.LANCE);
                    output.accept(ModItems.POISON_SWORD);
                    output.accept(ModItems.HAMMER);
                    output.accept(ModItems.WINGS_OF_DOOM);
                    output.accept(ModItems.SWIFTNESS_DAGGER);

                    //RITUAL//
                    output.accept(ModItems.SOUL_CORE);
            })
                .build());

    /// Loads the class, so the tab above is registered.
    public static void register() {
    }
}
