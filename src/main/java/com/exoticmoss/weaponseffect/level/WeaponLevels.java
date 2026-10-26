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
///  - Every normal hit (left-click) with the weapon gives XP_PER_NORMAL_HIT (small, so it can't be abused).
///  - The effects of the abilities count too (see EffectCredit): XP_PER_EFFECT_DAMAGE for every damage
///    the mob takes from Wither/Poison/burning/freezing, and half of XP_PER_KILL if it dies from it.
///  - When the weapon has XP_TO_NEXT_LEVEL experience it is "ready": the experience stops there and
///    the weapon must be taken to a Soul Core (tier = current level) to do the ritual and level up.
///  - The abilities read their numbers from tables: { level I, II, III, IV, V }.
public final class WeaponLevels {
    /// Highest level of a weapon.
    public static final int MAX_LEVEL = 5;
    /// Experience needed to be ready for the next level: { I -> II, II -> III, III -> IV, IV -> V }.
    public static final int[] XP_TO_NEXT_LEVEL = {1000, 2500, 4000, 8000};
    /// Experience given when an ability hits a mob, and extra experience when it kills the mob.
    public static final float XP_PER_HIT = 4F;
    public static final float XP_PER_KILL = 8F;
    /// Experience given by a normal hit (left-click) with the weapon.
    public static final float XP_PER_NORMAL_HIT = 1F;
    /// Experience for each damage a mob takes from the effect of an ability (Wither, Poison, fire, freezing).
    public static final float XP_PER_EFFECT_DAMAGE = 2F;

    private WeaponLevels() {
    }

    public static WeaponLevel get(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.WEAPON_LEVEL.get(), WeaponLevel.DEFAULT);
    }

    public static int level(ItemStack stack) {
        return Math.clamp(get(stack).level(), 1, MAX_LEVEL);
    }

    public static void set(ItemStack stack, int level, float xp) {
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

    /// The weapon of this kind the entity has: in the hands first, then anywhere in a player's inventory
    /// (the ability can still be hitting after the player changed item). Empty if none.
    public static ItemStack find(LivingEntity entity, Item weapon) {
        ItemStack stack = held(entity, weapon);
        if (stack.isEmpty() && entity instanceof Player player) {
            for (ItemStack slot : player.getInventory()) {
                if (slot.is(weapon)) return slot;
            }
        }
        return stack;
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
        addXp(owner, find(owner, weapon), XP_PER_HIT + (target.isDeadOrDying() ? XP_PER_KILL : 0));
        // The effects the ability gave (Wither, Poison, fire...) also give experience later
        EffectCredit.remember(owner, weapon, target);
    }

    /// Call it when a mob takes damage from the effect of an ability of this weapon (see EffectCredit).
    public static void rewardEffect(LivingEntity owner, Item weapon, boolean killed) {
        if (owner == null || owner.level().isClientSide()) return;
        addXp(owner, find(owner, weapon), killed ? XP_PER_KILL / 2 : XP_PER_EFFECT_DAMAGE);
    }

    /// Call it when the weapon hits a mob with a normal hit (left-click).
    public static void rewardNormalHit(ItemStack weapon, LivingEntity attacker) {
        if (attacker == null || attacker.level().isClientSide()) return;
        addXp(attacker, weapon, XP_PER_NORMAL_HIT);
    }

    /// The experience as text: "12" or "12.3".
    public static String formatXp(float xp) {
        return xp == Math.floor(xp) ? String.valueOf((int) xp) : String.format(java.util.Locale.ROOT, "%.1f", xp);
    }

    /// Adds experience to the weapon (stops when the weapon is ready for the next level).
    private static void addXp(LivingEntity owner, ItemStack stack, float gained) {
        if (stack.isEmpty() || !(stack.getItem() instanceof AbilityWeapon)) return;
        int level = level(stack);
        int needed = xpNeeded(level);
        float xp = get(stack).xp();
        if (needed == 0 || xp >= needed) return; // max level or already ready

        // Rounded to one decimal, so many small 0.1 steps add up exactly
        float newXp = Math.min(needed, Math.round((xp + gained) * 10) / 10F);
        set(stack, level, newXp);

        if (newXp >= needed && owner instanceof Player player) {
            player.sendOverlayMessage(Component.translatable("weaponseffect.level.ready", stack.getHoverName(), level + 1)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            owner.level().playSound(null, owner.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 0.6F);
        }
    }
}
