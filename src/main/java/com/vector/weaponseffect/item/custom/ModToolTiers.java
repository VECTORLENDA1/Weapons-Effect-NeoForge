package com.vector.weaponseffect.item.custom;

import com.google.common.base.Suppliers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import com.vector.weaponseffect.item.ModItems;
import com.vector.weaponseffect.util.ModTags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum ModToolTiers implements Tier {
    FIRE_SWORD(ModTags.Items.FIRE_SWORD, 1200, 1.0F, 1.0F, 12, () -> Ingredient.of(ModItems.FIRE_SWORD.get())),
    WITHER_SWORD(ModTags.Items.WITHER_SWORD, 1200, 1.0F, 1.0F, 15, () -> Ingredient.of(ModItems.WITHER_SWORD.get())),
    BLINDNESS_DAGGER(ModTags.Items.BLINDNESS_DAGGER, 1000, 1.0F, 1.0F, 8, () -> Ingredient.of(ModItems.BLINDNESS_DAGGER.get())),
    DARKNESS_MACE(ModTags.Items.DARKNESS_MACE, 1800, 1.0F, 1.0F, 18, () -> Ingredient.of(ModItems.DARKNESS_MACE.get())),
    GIMLIS_AXE(ModTags.Items.GIMLIS_AXE, 1600, 1.0F, 1.0F, 20, () -> Ingredient.of(ModItems.GIMLIS_AXE.get())),
    ICE_SWORD(ModTags.Items.ICE_SWORD, 1100, 1.0F, 1.0F, 13, () -> Ingredient.of(ModItems.ICE_SWORD.get())),
    LANCE(ModTags.Items.LANCE, 1200, 1.0F, 1.0F, 10, () -> Ingredient.of(ModItems.LANCE.get())),
    POISON_SWORD(ModTags.Items.POISON_SWORD, 1000, 1.0F, 1.0F, 11, () -> Ingredient.of(ModItems.POISON_SWORD.get())),
    HAMMER(ModTags.Items.HAMMER, 4000, 1.0F, 1.0F, 7, () -> Ingredient.of(ModItems.HAMMER.get())),
    WINGS_OF_DOOM(ModTags.Items.WINGS_OF_DOOM, 8000, 1.0F, 1.0F, 30, () -> Ingredient.of(ModItems.WINGS_OF_DOOM.get())),
    SWIFTNESS_DAGGER(ModTags.Items.SWIFTNESS_DAGGER, 700, 1.0F, 1.0F, 8, () -> Ingredient.of(ModItems.SWIFTNESS_DAGGER.get()));


    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    ModToolTiers(TagKey<Item> incorrectBlocksForDrops, int uses, float speed, float damage, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = Suppliers.memoize(repairIngredient::get);
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {return null;}

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

}

