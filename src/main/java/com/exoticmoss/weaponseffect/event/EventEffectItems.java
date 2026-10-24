package com.exoticmoss.weaponseffect.event;


import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.DamageTypeTags;
import com.exoticmoss.weaponseffect.WeaponsEffect;
import com.exoticmoss.weaponseffect.item.custom.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/// Effects the player gets while holding a weapon (main hand or off hand),
/// and the effects the weapons make the player immune to (main hand or off hand).
public class EventEffectItems {

    public static final String EFFECT_TAG = "ItemEffectApplied";

    /// The "EFFECT_TAG + effect" marks of the effects that were given by a weapon (saved with the player).
    private static final AttachmentType<Set<String>> APPLIED_EFFECTS = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(WeaponsEffect.MODID, "item_effects_applied"),
            Codec.STRING.listOf().xmap(list -> (Set<String>) new HashSet<>(list), ArrayList::new));

    /// Hooks (the player tick hooks are called from PlayerMixin, the effect hook from LivingEntityMixin).
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !onFireDamage(entity, source));
    }

    /// Effect given while holding the weapon -> amplifier of the effect for each weapon (0 = level I, 1 = level II...).
    /// If the player holds two weapons with the same effect, the first one of the list wins.
    private static final Map<Holder<MobEffect>, List<Map.Entry<Class<? extends Item>, Integer>>> HELD_EFFECTS = Map.of(
            MobEffects.FIRE_RESISTANCE, List.of(
                    Map.entry(FireSwordItem.class, 0),
                    Map.entry(WingsOfDoomItem.class, 0)),
            MobEffects.STRENGTH, List.of(
                    Map.entry(WingsOfDoomItem.class, 4),
                    Map.entry(GimlisAxeItem.class, 1),
                    Map.entry(DarknessMaceItem.class, 2),
                    Map.entry(HammerItem.class, 7),
                    Map.entry(LanceItem.class, 1)),
            MobEffects.SPEED, List.of(
                    Map.entry(WingsOfDoomItem.class, 2),
                    Map.entry(LanceItem.class, 1),
                    Map.entry(SwiftnessDaggerItem.class, 5)),
            MobEffects.RESISTANCE, List.of(
                    Map.entry(WingsOfDoomItem.class, 3),
                    Map.entry(GimlisAxeItem.class, 2),
                    Map.entry(DarknessMaceItem.class, 2),
                    Map.entry(WitherSwordItem.class, 0)),
            MobEffects.SATURATION, List.of(
                    Map.entry(WingsOfDoomItem.class, 1)),
            MobEffects.NIGHT_VISION, List.of(
                    Map.entry(WingsOfDoomItem.class, 0)),
            MobEffects.REGENERATION, List.of(
                    Map.entry(WingsOfDoomItem.class, 1))
    );

    /// Effect -> the weapons that make the player immune to it (in the right or left hand).
    private static final Map<Holder<MobEffect>, List<Class<? extends Item>>> IMMUNITIES = Map.of(
            MobEffects.BLINDNESS, List.of(BlindnessDaggerItem.class, WingsOfDoomItem.class),
            MobEffects.DARKNESS, List.of(DarknessMaceItem.class, WingsOfDoomItem.class),
            MobEffects.POISON, List.of(PoisonSwordItem.class, WingsOfDoomItem.class),
            MobEffects.SLOWNESS, List.of(LanceItem.class, WingsOfDoomItem.class, SwiftnessDaggerItem.class, IceSwordItem.class),
            MobEffects.WEAKNESS, List.of(WingsOfDoomItem.class),
            MobEffects.WITHER, List.of(WitherSwordItem.class, WingsOfDoomItem.class)
    );

    public static void onPlayerTickPre(Player player) {
            // Effects are given by the server (the client receives them automatically)
            if (player.level().isClientSide()) {
                return;
            }

            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            for (var entry : HELD_EFFECTS.entrySet()) {
                applyHeldEffect(player, mainHandItem, offHandItem, entry.getKey(), entry.getValue());
            }


            // Immunities: the effects are removed (and can't be given, see onEffectApplicable)
            for (var entry : IMMUNITIES.entrySet()) {
                if (holdsAny(player, entry.getValue())) {
                    removeIfPresent(player, entry.getKey());
                }
            }
    }

    /// Stops the effect from being given at all while a weapon that is immune to it is held
    /// (e.g. the Wither Boss attacks don't give Wither while holding the Wither Sword).
    /// Returns false when the effect must not be applied.
    public static boolean onEffectApplicable(LivingEntity entity, MobEffectInstance effect) {
        if (!(entity instanceof Player player)) return true;
        List<Class<? extends Item>> weapons = IMMUNITIES.get(effect.getEffect());
        return weapons == null || !holdsAny(player, weapons);
    }

    /// True if the player holds one of these weapons in the right hand or in the left hand (offhand).
    private static boolean holdsAny(Player player, List<Class<? extends Item>> weapons) {
        Item main = player.getMainHandItem().getItem();
        Item off = player.getOffhandItem().getItem();
        for (Class<? extends Item> weapon : weapons) {
            if (weapon.isInstance(main) || weapon.isInstance(off)) return true;
        }
        return false;
    }

    /// Gives the effect while one of the weapons is held, and removes it again (only if it was
    /// given by a weapon, never an effect from a potion) when no weapon with that effect is held.
    private static void applyHeldEffect(Player player, ItemStack mainHand, ItemStack offHand, Holder<MobEffect> effect,
                                        List<Map.Entry<Class<? extends Item>, Integer>> amplifiers) {
        int amplifier = -1;
        for (Map.Entry<Class<? extends Item>, Integer> entry : amplifiers) {
            if (entry.getKey().isInstance(mainHand.getItem()) || entry.getKey().isInstance(offHand.getItem())) {
                amplifier = entry.getValue();
                break;
            }
        }

        String tag = EFFECT_TAG + effect.getRegisteredName();
        if (amplifier >= 0) {
            MobEffectInstance currentEffect = player.getEffect(effect);
            // Infinite time while the weapon is held (it is removed when the weapon is no longer held)
            if (currentEffect == null || currentEffect.getAmplifier() < amplifier
                    || (currentEffect.getAmplifier() == amplifier && !currentEffect.isInfiniteDuration())) {
                player.addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, amplifier, false, false, true));
                setMark(player, tag, true);
            }
        } else if (hasMark(player, tag)) {
            player.removeEffect(effect);
            setMark(player, tag, false);
        }
    }

    private static boolean hasMark(Player player, String tag) {
        Set<String> marks = player.getAttached(APPLIED_EFFECTS);
        return marks != null && marks.contains(tag);
    }

    private static void setMark(Player player, String tag, boolean value) {
        Set<String> marks = player.getAttached(APPLIED_EFFECTS);
        Set<String> copy = marks == null ? new HashSet<>() : new HashSet<>(marks);
        if (value ? !copy.add(tag) : !copy.remove(tag)) return;
        if (copy.isEmpty()) player.removeAttached(APPLIED_EFFECTS);
        else player.setAttached(APPLIED_EFFECTS, copy);
    }

    // ---------------------------------------------------------------- fire and lava immunity

    /// True if the player holds a weapon that makes them completely immune to fire and lava
    /// (Fire Sword or Wings of Doom, in the right or left hand).
    public static boolean isFireProof(Player player) {
        return isFireProofItem(player.getMainHandItem()) || isFireProofItem(player.getOffhandItem());
    }

    private static boolean isFireProofItem(ItemStack stack) {
        return stack.getItem() instanceof FireSwordItem || stack.getItem() instanceof WingsOfDoomItem;
    }

    /// The player never catches fire: no fire on the screen and no flames on the body
    /// (on both sides, so the client doesn't show the fire even for a moment).
    public static void onPlayerTickPost(Player player) {
        if (!isFireProof(player)) return;

        player.clearFire();
        if (!player.level().isClientSide()) {
            player.setSharedFlagOnFire(false);
        }
    }

    /// No damage at all from fire, lava, magma blocks, fireballs...
    /// Returns true when the damage must be cancelled.
    public static boolean onFireDamage(LivingEntity entity, DamageSource source) {
        return entity instanceof Player player && isFireProof(player) && source.is(DamageTypeTags.IS_FIRE);
    }

    private static void removeIfPresent(Player player, Holder<MobEffect> effect) {
        if (player.hasEffect(effect)) {
            player.removeEffect(effect);
        }
    }
}
