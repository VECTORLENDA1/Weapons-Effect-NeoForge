package com.exoticmoss.weaponseffect.item;

import com.exoticmoss.weaponseffect.util.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

/// Materials of the weapons (durability, extra damage and enchantability).
/// Each weapon is repaired with itself: the "repair" tag of each material
/// (data/weaponseffect/tags/item/<weapon>.json) only contains that weapon.
public class ModToolTiers {
    //                                                                       incorrect blocks for drops                  uses  speed  damage  enchant  repair item
    public static final ToolMaterial FIRE_SWORD = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1200, 1.0F, 1.0F, 12, ModTags.Items.FIRE_SWORD);
    public static final ToolMaterial WITHER_SWORD = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1200, 1.0F, 1.0F, 15, ModTags.Items.WITHER_SWORD);
    public static final ToolMaterial BLINDNESS_DAGGER = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1000, 1.0F, 1.0F, 8, ModTags.Items.BLINDNESS_DAGGER);
    public static final ToolMaterial DARKNESS_MACE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1800, 1.0F, 1.0F, 18, ModTags.Items.DARKNESS_MACE);
    public static final ToolMaterial GIMLIS_AXE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1600, 1.0F, 1.0F, 20, ModTags.Items.GIMLIS_AXE);
    public static final ToolMaterial ICE_SWORD = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1100, 1.0F, 1.0F, 13, ModTags.Items.ICE_SWORD);
    public static final ToolMaterial LANCE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1200, 1.0F, 1.0F, 10, ModTags.Items.LANCE);
    public static final ToolMaterial POISON_SWORD = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1000, 1.0F, 1.0F, 11, ModTags.Items.POISON_SWORD);
    public static final ToolMaterial HAMMER = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4000, 1.0F, 1.0F, 7, ModTags.Items.HAMMER);
    public static final ToolMaterial WINGS_OF_DOOM = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 8000, 1.0F, 1.0F, 30, ModTags.Items.WINGS_OF_DOOM);
    public static final ToolMaterial SWIFTNESS_DAGGER = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 700, 1.0F, 1.0F, 8, ModTags.Items.SWIFTNESS_DAGGER);
}
