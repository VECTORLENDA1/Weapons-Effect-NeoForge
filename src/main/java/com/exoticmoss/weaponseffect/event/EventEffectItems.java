package com.exoticmoss.weaponseffect.event;


import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.exoticmoss.weaponseffect.WeaponsEffect;
import com.exoticmoss.weaponseffect.item.custom.*;

import java.util.List;
import java.util.Map;

/// Effects the player gets while holding a weapon (main hand or off hand),
/// and the effects the weapons make the player immune to (main hand).
@EventBusSubscriber(modid = WeaponsEffect.MODID)
public class EventEffectItems {

    public static final String EFFECT_TAG = "ItemEffectApplied";

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

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
            Player player = event.getEntity();
            // Effects are given by the server (the client receives them automatically)
            if (player.level().isClientSide()) {
                return;
            }

            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            for (var entry : HELD_EFFECTS.entrySet()) {
                applyHeldEffect(player, mainHandItem, offHandItem, entry.getKey(), entry.getValue());
            }


            //Immunities//
            boolean hasBlindnessDaggerItem = mainHandItem.getItem() instanceof BlindnessDaggerItem;
            boolean hasDarknessMaceItem = mainHandItem.getItem() instanceof DarknessMaceItem;
            boolean hasIceSwordItem = mainHandItem.getItem() instanceof IceSwordItem;
            boolean hasLanceItem = mainHandItem.getItem() instanceof LanceItem;
            boolean hasPoisonSwordItem = mainHandItem.getItem() instanceof PoisonSwordItem;
            boolean hasSwiftnessDaggerItem = mainHandItem.getItem() instanceof SwiftnessDaggerItem;
            boolean hasWingsofDoomItem = mainHandItem.getItem() instanceof WingsOfDoomItem;
            boolean hasWitherSwordItem = mainHandItem.getItem() instanceof WitherSwordItem;

            //Blindness//
            if (hasBlindnessDaggerItem || hasWingsofDoomItem) {
                removeIfPresent(player, MobEffects.BLINDNESS);
            }

            //Darkness//
            if (hasDarknessMaceItem || hasWingsofDoomItem) {
                removeIfPresent(player, MobEffects.DARKNESS);
            }

            //Poison//
            if (hasPoisonSwordItem || hasWingsofDoomItem) {
                removeIfPresent(player, MobEffects.POISON);
            }

            //Slowness//
            if (hasLanceItem || hasWingsofDoomItem || hasSwiftnessDaggerItem || hasIceSwordItem) {
                removeIfPresent(player, MobEffects.SLOWNESS);
            }

            //Weakness//
            if (hasWingsofDoomItem) {
                removeIfPresent(player, MobEffects.WEAKNESS);
            }

            //Wither//
            if (hasWitherSwordItem || hasWingsofDoomItem) {
                removeIfPresent(player, MobEffects.WITHER);
            }
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
            if (currentEffect == null || currentEffect.getAmplifier() < amplifier) {
                player.addEffect(new MobEffectInstance(effect, 12000, amplifier, false, false, true));
                player.getPersistentData().putBoolean(tag, true);
            }
        } else if (player.getPersistentData().getBooleanOr(tag, false)) {
            player.removeEffect(effect);
            player.getPersistentData().remove(tag);
        }
    }

    private static void removeIfPresent(Player player, Holder<MobEffect> effect) {
        if (player.hasEffect(effect)) {
            player.removeEffect(effect);
        }
    }
}
