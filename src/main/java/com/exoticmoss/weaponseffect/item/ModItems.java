package com.exoticmoss.weaponseffect.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.Rarity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import com.exoticmoss.weaponseffect.item.custom.*;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.exoticmoss.weaponseffect.WeaponsEffect.MODID;


public class ModItems {
    /// Registers an item (the id is set on its properties, as vanilla requires).
    private static Item registerItem(String name, Function<Item.Properties, ? extends Item> factory, UnaryOperator<Item.Properties> properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.apply(new Item.Properties().setId(key))));
    }



    //WEAPONS//
    // .sword(material, attack damage, attack speed) -> the damage shown in game is: attack damage + 1 (material) + 1 (hand)
    public static final Item FIRE_SWORD = registerItem("fire_sword", FireSwordItem::new,
            p -> p.fireResistant().sword(ModToolTiers.FIRE_SWORD, 8, -2.2F));
    public static final Item WITHER_SWORD = registerItem("wither_sword", WitherSwordItem::new,
            p -> p.sword(ModToolTiers.WITHER_SWORD, 8, -2.2F));
    public static final Item BLINDNESS_DAGGER = registerItem("blindness_dagger", BlindnessDaggerItem::new,
            p -> p.sword(ModToolTiers.BLINDNESS_DAGGER, 4, -0.3F));
    public static final Item DARKNESS_MACE = registerItem("darkness_mace", DarknessMaceItem::new,
            p -> p.sword(ModToolTiers.DARKNESS_MACE, 13, -2.8F));
    public static final Item GIMLIS_AXE = registerItem("gimlis_axe", GimlisAxeItem::new,
            p -> p.sword(ModToolTiers.GIMLIS_AXE, 10, -2.6F));
    public static final Item ICE_SWORD = registerItem("ice_sword", IceSwordItem::new,
            p -> p.sword(ModToolTiers.ICE_SWORD, 8, -2.2F));
    public static final Item LANCE = registerItem("lance", LanceItem::new,
            p -> p.sword(ModToolTiers.LANCE, 6, -1.3F));
    public static final Item POISON_SWORD = registerItem("poison_sword", PoisonSwordItem::new,
            p -> p.sword(ModToolTiers.POISON_SWORD, 8, -2.2F));
    public static final Item HAMMER = registerItem("hammer", HammerItem::new,
            p -> p.sword(ModToolTiers.HAMMER, 18, -3.0F));
    public static final Item WINGS_OF_DOOM = registerItem("wings_of_doom", WingsOfDoomItem::new,
            p -> p.fireResistant().sword(ModToolTiers.WINGS_OF_DOOM, 48, -0F));
    public static final Item SWIFTNESS_DAGGER = registerItem("swiftness_dagger", SwiftnessDaggerItem::new,
            p -> p.sword(ModToolTiers.SWIFTNESS_DAGGER, 4, -0.3F));


    //RITUAL//
    /// Damage the Soul Core dropped as an item never takes (fire, lava, explosions, lightning, cactus...).
    /// The list is in data/weaponseffect/tags/damage_type/soul_core_immune.json
    public static final TagKey<DamageType> SOUL_CORE_IMMUNE =
            TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MODID, "soul_core_immune"));

    /// Spent in the Soul Ritual that raises the level of a weapon (see SoulRitualEntity).
    public static final Item SOUL_CORE = registerItem("soul_core", Item::new,
            p -> p.stacksTo(16).rarity(Rarity.EPIC).delayedComponent(DataComponents.DAMAGE_RESISTANT,
                    context -> new DamageResistant(context.getOrThrow(SOUL_CORE_IMMUNE))));

    /// Loads the class, so all the items above are registered.
    public static void Register() {
    }
}
