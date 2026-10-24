package com.exoticmoss.weaponseffect.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/// Experience from the effects of the abilities (Wither, Poison, fire, freezing...).
///
/// When an ability hits a mob (WeaponLevels.reward), the mob remembers which player and weapon hit it,
/// for CREDIT_SECONDS. While it remembers:
///  - every time the mob takes damage from Wither, Poison, burning or freezing, the weapon gets
///    WeaponLevels.XP_PER_EFFECT_DAMAGE;
///  - if the mob dies from one of those, the weapon gets half of WeaponLevels.XP_PER_KILL.
public final class EffectCredit {
    /// How long a mob remembers the last ability that hit it (seconds). Longer than the longest effect.
    public static final int CREDIT_SECONDS = 30;

    private record Credit(UUID owner, Item weapon, long until) {
    }

    /// Mob -> who gets the experience of its effects.
    private static final Map<UUID, Credit> CREDITS = new HashMap<>();

    private EffectCredit() {
    }

    /// The mob was hit by an ability of this weapon.
    public static void remember(LivingEntity owner, Item weapon, LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        long now = level.getGameTime();
        CREDITS.put(target.getUUID(), new Credit(owner.getUUID(), weapon, now + CREDIT_SECONDS * 20L));
        if (CREDITS.size() > 512) CREDITS.values().removeIf(credit -> credit.until() < now);
    }

    /// The mob took damage (called after the damage).
    public static void onDamage(LivingEntity mob, DamageSource source, float amount) {
        if (amount <= 0 || mob.isDeadOrDying() || !isEffectDamage(mob, source)) return;
        LivingEntity owner = owner(mob);
        if (owner != null) WeaponLevels.rewardEffect(owner, CREDITS.get(mob.getUUID()).weapon(), false);
    }

    /// The mob died.
    public static void onDeath(LivingEntity mob, DamageSource source) {
        if (isEffectDamage(mob, source)) {
            LivingEntity owner = owner(mob);
            if (owner != null) WeaponLevels.rewardEffect(owner, CREDITS.get(mob.getUUID()).weapon(), true);
        }
        CREDITS.remove(mob.getUUID());
    }

    /// Damage that comes from an effect: Wither, Poison, burning (after the flames) or freezing.
    private static boolean isEffectDamage(LivingEntity mob, DamageSource source) {
        if (source.is(DamageTypes.WITHER)) return mob.hasEffect(MobEffects.WITHER);
        if (source.is(DamageTypes.MAGIC)) return mob.hasEffect(MobEffects.POISON);
        return source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypeTags.IS_FREEZING);
    }

    /// The player who gets the experience (null if none, too old, or not in the game).
    private static LivingEntity owner(LivingEntity mob) {
        Credit credit = CREDITS.get(mob.getUUID());
        if (credit == null || !(mob.level() instanceof ServerLevel level)) return null;
        if (credit.until() < level.getGameTime()) {
            CREDITS.remove(mob.getUUID());
            return null;
        }
        Entity owner = level.getServer().getPlayerList().getPlayer(credit.owner());
        if (owner == null) owner = level.getEntity(credit.owner());
        return owner instanceof LivingEntity living && living.isAlive() ? living : null;
    }
}
