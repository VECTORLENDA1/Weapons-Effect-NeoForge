package com.vector.weaponseffect.item;

import com.vector.weaponseffect.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.vector.weaponseffect.item.custom.*;

import static com.vector.weaponseffect.WeaponsEffect.MODID;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);



    //WEAPONS//
    public static final DeferredItem<Item> FIRE_SWORD = ITEMS.register("fire_sword",
            () -> new FireSwordItem(ModToolTiers.FIRE_SWORD, new Item.Properties().fireResistant()
                    .attributes(SwordItem.createAttributes(ModToolTiers.FIRE_SWORD,8,-2.2F))));
    public static final DeferredItem<Item> WITHER_SWORD = ITEMS.register("wither_sword",
            () -> new WitherSwordItem(ModToolTiers.WITHER_SWORD, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.WITHER_SWORD, 8, -2.2F))));
    public static final DeferredItem<Item> BLINDNESS_DAGGER = ITEMS.register("blindness_dagger",
            () -> new BlindnessDaggerItem(ModToolTiers.BLINDNESS_DAGGER, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.BLINDNESS_DAGGER, 4, -0.3F))));
    public static final DeferredItem<Item> DARKNESS_MACE = ITEMS.register("darkness_mace",
            () -> new DarknessMaceItem(ModToolTiers.DARKNESS_MACE, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.DARKNESS_MACE, 13, -2.8F))));
    public static final DeferredItem<Item> GIMLIS_AXE = ITEMS.register("gimlis_axe",
            () -> new GimlisAxeItem(ModToolTiers.GIMLIS_AXE, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.GIMLIS_AXE, 10, -2.6F))));
    public static final DeferredItem<Item> ICE_SWORD = ITEMS.register("ice_sword",
            () -> new IceSwordItem(ModToolTiers.ICE_SWORD, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.ICE_SWORD, 8, -2.2F))));
    public static final DeferredItem<Item> LANCE = ITEMS.register("lance",
            () -> new LanceItem(ModToolTiers.LANCE, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.LANCE, 6, -1.3F))));
    public static final DeferredItem<Item> POISON_SWORD = ITEMS.register("poison_sword",
            () -> new PoisonSwordItem(ModToolTiers.POISON_SWORD, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.POISON_SWORD, 8, -2.2F))));
    public static final DeferredItem<Item> HAMMER = ITEMS.register("hammer",
            () -> new HammerItem(ModToolTiers.HAMMER, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.HAMMER, 18, -3.0F))));
    public static final DeferredItem<Item> WINGS_OF_DOOM = ITEMS.register("wings_of_doom",
            () -> new WingsOfDoomItem(ModToolTiers.WINGS_OF_DOOM, new Item.Properties().fireResistant()
                    .attributes(SwordItem.createAttributes(ModToolTiers.WINGS_OF_DOOM, 48, -0F))));
    public static final DeferredItem<Item> SWIFTNESS_DAGGER = ITEMS.register("swiftness_dagger",
            () -> new SwiftnessDaggerItem(ModToolTiers.SWIFTNESS_DAGGER, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.SWIFTNESS_DAGGER, 4, -0.3F))));


    public static void Register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
