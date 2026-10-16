package com.exoticmoss.weaponseffect.level;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/// Levels of the weapons and their ability experience.
///
/// HOW IT WORKS
///  - Every time an ability hits a mob, the weapon gets XP_PER_HIT; if the mob dies, also XP_PER_KILL.
///  - When the weapon has XP_TO_NEXT_LEVEL experience it is "ready": the experience stops there and
///    the weapon must be taken to a Soul Core (tier = current level) to do the ritual and level up.
///  - The abilities read their numbers from tables: { level I, II, III, IV, V }.
public final class WeaponLevels {
    /// Highest level of a weapon.
    public static final int MAX_LEVEL = 5;
    /// Experience needed to be ready for the next level: { I -> II, II -> III, III -> IV, IV -> V }.
    public static final int[] XP_TO_NEXT_LEVEL = {500, 1500, 3000, 6000};
    /// Experience given when an ability hits a mob, and extra experience when it kills the mob.
    public static final int XP_PER_HIT = 2;
    public static final int XP_PER_KILL = 10;

    private WeaponLevels() {
    }

    public static WeaponLevel get(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.WEAPON_LEVEL.get(), WeaponLevel.DEFAULT);
    }

    public static int level(ItemStack stack) {
        return Math.clamp(get(stack).level(), 1, MAX_LEVEL);
    }

    public static void set(ItemStack stack, int level, int xp) {
        stack.set(ModDataComponents.WEAPON_LEVEL.get(), new WeaponLevel(Math.clamp(level, 1, MAX_LEVEL), Math.max(0, xp)));
    }

    /// Experience needed for the next level (0 at the max level).
    public static int xpNeeded(int level) {
        return level >= MAX_LEVEL ? 0 : XP_TO_NEXT_LEVEL[level - 1];
    }

    /// True if the weapon has all the experience for the next level (it can go to a Soul Core).
    public static boolean isReady(ItemStack stack) {
        int level = level(stack);
        return level < MAX_LEVEL && get(stack).xp() >= xpNeeded(level);
    }

    /// The value of a table for this level ({ level I, II, III, IV, V }).
    public static int pick(int[] table, int level) {
        return table[Math.clamp(level, 1, table.length) - 1];
    }

    public static float pick(float[] table, int level) {
        return table[Math.clamp(level, 1, table.length) - 1];
    }

    public static double pick(double[] table, int level) {
        return table[Math.clamp(level, 1, table.length) - 1];
    }

    /// The weapon of this kind the entity is holding (right hand first, then left). Empty if none.
    public static ItemStack held(LivingEntity entity, Item weapon) {
        if (entity.getMainHandItem().is(weapon)) return entity.getMainHandItem();
        if (entity.getOffhandItem().is(weapon)) return entity.getOffhandItem();
        return ItemStack.EMPTY;
    }

    /// Level of the weapon of this kind the entity is holding (1 if it has none).
    public static int levelOf(LivingEntity entity, Item weapon) {
        ItemStack stack = held(entity, weapon);
        return stack.isEmpty() ? 1 : level(stack);
    }

    /// Call it when an ability of this weapon hits a mob (after the damage).
    /// The weapon (in the hand of the owner) gets experience.
    public static void reward(LivingEntity owner, Item weapon, LivingEntity target) {
        if (owner == null || target == owner || owner.level().isClientSide()) return;
        ItemStack stack = held(owner, weapon);
        if (stack.isEmpty()) return;

        int level = level(stack);
        int needed = xpNeeded(level);
        int xp = get(stack).xp();
        if (needed == 0 || xp >= needed) return; // max level or already ready

        int gained = XP_PER_HIT + (target.isDeadOrDying() ? XP_PER_KILL : 0);
        int newXp = Math.min(needed, xp + gained);
        set(stack, level, newXp);

        if (newXp >= needed && owner instanceof Player player) {
            player.sendOverlayMessage(Component.translatable("weaponseffect.level.ready", stack.getHoverName(), level + 1)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            owner.level().playSound(null, owner.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 0.6F);
        }
    }
}
