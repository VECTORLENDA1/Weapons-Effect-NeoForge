package com.exoticmoss.weaponseffect.level;

import com.exoticmoss.weaponseffect.entity.BlackHoleEntity;
import com.exoticmoss.weaponseffect.entity.custom.*;
import com.exoticmoss.weaponseffect.item.ModItems;
import com.exoticmoss.weaponseffect.item.custom.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntToDoubleFunction;

/// The message in the chat when a weapon goes up a level: what is new and what got stronger.
///  - The numbers are read from the tables of each ability, so they are always right
///    (a number that was 0 before is "new": it is explained by the text of that level instead).
///  - The texts of the new things are in the lang files: "weaponseffect.levelup.<weapon>.<level>"
///    (only the levels in NEWS have one).
public final class LevelUpChanges {
    /// Levels that bring something new for every weapon (they have a text in the lang files).
    private static final int[] NEWS = {3, 5};

    /// One number that changes with the level: its name in the lang files ("weaponseffect.stat.<name>"),
    /// its value for a level, and the unit shown after it ("s" for seconds, "" for nothing).
    private record Stat(String name, IntToDoubleFunction value, String unit) {
    }

    private static Map<Item, List<Stat>> stats;

    private LevelUpChanges() {
    }

    // ---------------------------------------------------------------- the numbers of each weapon

    private static Map<Item, List<Stat>> stats() {
        if (stats != null) return stats;
        stats = new HashMap<>();
        stats.put(ModItems.FIRE_SWORD.get(), List.of(
                stat("damage", l -> WeaponLevels.pick(FlameWaveEntity.DAMAGE, l)),
                stat("length", l -> WeaponLevels.pick(FlameWaveEntity.LENGTH, l)),
                stat("width", l -> WeaponLevels.pick(FlameWaveEntity.WIDTH, l)),
                seconds("burn", l -> WeaponLevels.pick(FlameWaveEntity.FIRE_SECONDS, l)),
                seconds("ground_fire", l -> WeaponLevels.pick(FlameWaveEntity.GROUND_FIRE_SECONDS, l)),
                stat("ground_fire_damage", l -> WeaponLevels.pick(FlameWaveEntity.GROUND_FIRE_DAMAGE, l)),
                cooldown(FireSwordItem.COOLDOWN_TICKS)));
        stats.put(ModItems.WITHER_SWORD.get(), List.of(
                stat("skulls", l -> WeaponLevels.pick(WitherRainSkull.SKULL_COUNT, l)),
                stat("damage", l -> WeaponLevels.pick(WitherRainSkull.DAMAGE, l)),
                seconds("wither", l -> WeaponLevels.pick(WitherRainSkull.WITHER_SECONDS, l)),
                stat("wither_level", l -> WeaponLevels.pick(WitherRainSkull.WITHER_AMPLIFIER, l) + 1),
                stat("explosion_radius", l -> WeaponLevels.pick(WitherRainSkull.EXPLOSION_RADIUS, l)),
                stat("explosion_damage", l -> WeaponLevels.pick(WitherRainSkull.EXPLOSION_DAMAGE, l)),
                cooldown(WitherSwordItem.COOLDOWN_TICKS)));
        stats.put(ModItems.ICE_SWORD.get(), List.of(
                stat("shards", l -> WeaponLevels.pick(IceShardEntity.SHARD_COUNT, l)),
                stat("damage", l -> WeaponLevels.pick(IceShardEntity.DAMAGE, l)),
                seconds("freeze", l -> WeaponLevels.pick(IceShardEntity.FREEZE_SECONDS, l)),
                stat("slowness_level", l -> WeaponLevels.pick(IceShardEntity.SLOWNESS_AMPLIFIER, l) + 1),
                stat("area_freeze_radius", l -> WeaponLevels.pick(IceShardEntity.AREA_FREEZE_RADIUS, l)),
                cooldown(IceSwordItem.COOLDOWN_TICKS)));
        stats.put(ModItems.LANCE.get(), List.of(
                stat("lances", l -> WeaponLevels.pick(LanceProjectileEntity.RAIN_COUNT, l)),
                stat("damage", l -> WeaponLevels.pick(LanceProjectileEntity.RAIN_DAMAGE, l)),
                stat("radius", l -> WeaponLevels.pick(LanceProjectileEntity.RAIN_RADIUS, l)),
                seconds("weakness", l -> WeaponLevels.pick(LanceProjectileEntity.WEAKNESS_SECONDS, l)),
                stat("giant_damage", l -> WeaponLevels.pick(LanceProjectileEntity.GIANT_LANCE, l) ? LanceProjectileEntity.giantDamage(l) : 0),
                stat("giant_radius", l -> WeaponLevels.pick(LanceProjectileEntity.GIANT_LANCE, l) ? LanceProjectileEntity.giantRadius(l) : 0)));
        stats.put(ModItems.POISON_SWORD.get(), List.of(
                stat("radius", l -> WeaponLevels.pick(PoisonCloudEntity.RADIUS, l)),
                seconds("duration", l -> WeaponLevels.pick(PoisonCloudEntity.DURATION, l) / 20.0),
                seconds("poison", l -> WeaponLevels.pick(PoisonCloudEntity.POISON_SECONDS, l)),
                stat("poison_level", l -> WeaponLevels.pick(PoisonCloudEntity.POISON_AMPLIFIER, l) + 1),
                cooldown(PoisonSwordItem.COOLDOWN_TICKS)));
        stats.put(ModItems.HAMMER.get(), List.of(
                stat("radius", l -> WeaponLevels.pick(HammerItem.EXPLOSION_RADIUS, l)),
                stat("damage", l -> WeaponLevels.pick(HammerItem.EXPLOSION_DAMAGE, l)),
                stat("knockback", l -> WeaponLevels.pick(HammerItem.KNOCKBACK, l)),
                seconds("stun", l -> WeaponLevels.pick(HammerItem.STUN_SECONDS, l)),
                cooldown(HammerItem.COOLDOWN_TICKS)));
        stats.put(ModItems.WINGS_OF_DOOM.get(), List.of(
                stat("radius", l -> WeaponLevels.pick(BlackHoleEntity.MAX_RADIUS, l)),
                seconds("duration", l -> WeaponLevels.pick(BlackHoleEntity.LIFETIME, l) / 20.0),
                stat("pull_range", l -> WeaponLevels.pick(BlackHoleEntity.PULL_RANGE, l)),
                cooldown(WingsOfDoomItem.COOLDOWN_TICKS)));
        stats.put(ModItems.BLINDNESS_DAGGER.get(), List.of(
                stat("radius", l -> WeaponLevels.pick(SmokeBombEntity.RADIUS, l)),
                seconds("duration", l -> WeaponLevels.pick(SmokeBombEntity.SMOKE_SECONDS, l)),
                seconds("blindness", l -> WeaponLevels.pick(SmokeBombEntity.BLINDNESS_SECONDS, l)),
                seconds("invisibility", l -> WeaponLevels.pick(SmokeBombEntity.INVISIBILITY_SECONDS, l)),
                cooldown(BlindnessDaggerItem.COOLDOWN_TICKS)));
        stats.put(ModItems.SWIFTNESS_DAGGER.get(), List.of(
                stat("distance", l -> WeaponLevels.pick(DashEntity.DISTANCE, l)),
                stat("damage", l -> WeaponLevels.pick(DashEntity.DAMAGE, l)),
                seconds("poison", l -> WeaponLevels.pick(DashEntity.POISON_SECONDS, l)),
                cooldown(SwiftnessDaggerItem.COOLDOWN_TICKS)));
        stats.put(ModItems.GIMLIS_AXE.get(), List.of(
                stat("range", l -> WeaponLevels.pick(ThrownAxeEntity.RANGE, l)),
                stat("damage", l -> WeaponLevels.pick(ThrownAxeEntity.DAMAGE, l)),
                seconds("slowness", l -> WeaponLevels.pick(ThrownAxeEntity.SLOWNESS_SECONDS, l)),
                stat("axes", l -> WeaponLevels.pick(ThrownAxeEntity.AXE_COUNT, l)),
                cooldown(GimlisAxeItem.COOLDOWN_TICKS)));
        stats.put(ModItems.DARKNESS_MACE.get(), List.of(
                stat("radius", l -> WeaponLevels.pick(DarknessSlamEntity.SLAM_RADIUS, l)),
                stat("damage", l -> WeaponLevels.pick(DarknessSlamEntity.SLAM_DAMAGE, l)),
                stat("zone_radius", l -> WeaponLevels.pick(DarknessSlamEntity.ZONE_RADIUS, l)),
                seconds("zone_duration", l -> WeaponLevels.pick(DarknessSlamEntity.ZONE_SECONDS, l)),
                seconds("disoriented", l -> WeaponLevels.pick(DarknessSlamEntity.DISORIENTED_SECONDS, l)),
                cooldown(DarknessMaceItem.COOLDOWN_TICKS)));
        return stats;
    }

    private static Stat stat(String name, IntToDoubleFunction value) {
        return new Stat(name, value, "");
    }

    private static Stat seconds(String name, IntToDoubleFunction value) {
        return new Stat(name, value, "s");
    }

    private static Stat cooldown(int[] ticks) {
        return new Stat("cooldown", l -> WeaponLevels.pick(ticks, l) / 20.0, "s");
    }

    // ---------------------------------------------------------------- the message

    /// Tells the player that the weapon went from level "from" to level "to", and what changed.
    public static void tell(Player player, ItemStack weapon, int from, int to) {
        player.sendSystemMessage(Component.translatable("weaponseffect.ritual.success", weapon.getHoverName(), to)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        String id = BuiltInRegistries.ITEM.getKey(weapon.getItem()).getPath();

        for (int level = from + 1; level <= to; level++) {
            // What is new at this level
            for (int news : NEWS) {
                if (news == level) {
                    player.sendSystemMessage(Component.translatable("weaponseffect.levelup.new",
                            Component.translatable("weaponseffect.levelup." + id + "." + level)).withStyle(ChatFormatting.AQUA));
                }
            }
        }
        // What got stronger (only numbers that already existed before)
        for (Stat stat : stats().getOrDefault(weapon.getItem(), List.of())) {
            double before = stat.value().applyAsDouble(from);
            double after = stat.value().applyAsDouble(to);
            if (before == 0 || before == after) continue;
            player.sendSystemMessage(Component.translatable("weaponseffect.levelup.stat",
                    Component.translatable("weaponseffect.stat." + stat.name()),
                    format(before) + stat.unit(), format(after) + stat.unit()).withStyle(ChatFormatting.GRAY));
        }
    }

    /// "8" or "8.5" (no ".0").
    private static String format(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.2f", value).replaceAll("0$", "");
    }
}
