package com.vector.weaponseffect.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.vector.weaponseffect.block.ModBlocks;

import static com.vector.weaponseffect.WeaponsEffect.MODID;


//Isto serve para cria um guia no modo creativo dop jogo, para os teus items
public class ModCreativeModTabs {
    public static final net.neoforged.neoforge.registries.DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            net.neoforged.neoforge.registries.DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WEAPONS_EFFECT = CREATIVE_MODE_TABS.register("weapons_effect",
            () -> CreativeModeTab.builder()
            .title(Component.translatable("creativetab.weapons_effect"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.WINGS_OF_DOOM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {


                    //WEAPONS//
                    output.accept(ModItems.FIRE_SWORD.get());
                    output.accept(ModItems.WITHER_SWORD.get());
                    output.accept(ModItems.BLINDNESS_DAGGER.get());
                    output.accept(ModItems.DARKNESS_MACE.get());
                    output.accept(ModItems.GIMLIS_AXE.get());
                    output.accept(ModItems.ICE_SWORD.get());
                    output.accept(ModItems.LANCE.get());
                    output.accept(ModItems.POISON_SWORD.get());
                    output.accept(ModItems.HAMMER.get());
                    output.accept(ModItems.WINGS_OF_DOOM.get());
                    output.accept(ModItems.SWIFTNESS_DAGGER.get());
            })
                .build());

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
