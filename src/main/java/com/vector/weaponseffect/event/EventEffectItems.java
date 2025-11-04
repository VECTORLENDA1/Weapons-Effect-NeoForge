package com.vector.weaponseffect.event;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.vector.weaponseffect.item.custom.*;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = "weaponseffect")
public class EventEffectItems {

    public static final String EFFECT_TAG = "ItemEffectApplied";

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
            Player player = event.getEntity();
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            boolean hasBlindnessDaggerItem = mainHandItem.getItem() instanceof BlindnessDaggerItem;
            boolean hasDarknessMaceItem = mainHandItem.getItem() instanceof DarknessMaceItem;
            boolean hasFireSwordItem = mainHandItem.getItem() instanceof FireSwordItem;
            boolean hasGimlisAxeItem = mainHandItem.getItem() instanceof GimlisAxeItem;
            boolean hasIceSwordItem = mainHandItem.getItem() instanceof IceSwordItem;
            boolean hasLanceItem = mainHandItem.getItem() instanceof LanceItem;
            boolean hasPoisonSwordItem = mainHandItem.getItem() instanceof PoisonSwordItem;
            boolean hashHammerItem = mainHandItem.getItem() instanceof HammerItem;
            boolean hasSwiftnessDaggerItem = mainHandItem.getItem() instanceof SwiftnessDaggerItem;
            boolean hasWingsofDoomItem = mainHandItem.getItem() instanceof WingsOfDoomItem;
            boolean hasWitherSwordItem = mainHandItem.getItem() instanceof WitherSwordItem;


            //Fire Resistance//
            Map<Class<? extends Item>, Integer> fireAmplifiers = new HashMap<>();
            fireAmplifiers.put(FireSwordItem.class, 0);
            fireAmplifiers.put(WingsOfDoomItem.class, 0);


            int fireAmplifier = -1;

            for (Map.Entry<Class<? extends Item>, Integer> entry : fireAmplifiers.entrySet()) {
                if (entry.getKey().isInstance(mainHandItem.getItem()) || entry.getKey().isInstance(offHandItem.getItem())) {
                    fireAmplifier = entry.getValue();
                    break;
                }
            }
            if (fireAmplifier >= 0) {
                MobEffectInstance currentEffect = player.getEffect(MobEffects.FIRE_RESISTANCE);
                if (currentEffect == null || currentEffect.getAmplifier() < fireAmplifier) {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, fireAmplifier, false, false, true));
                    player.getPersistentData().putBoolean(EFFECT_TAG + MobEffects.FIRE_RESISTANCE.getRegisteredName().toString(), true);
                }
            } else if (player.getPersistentData().getBoolean(EFFECT_TAG + MobEffects.FIRE_RESISTANCE.getRegisteredName().toString())) {
                player.removeEffect(MobEffects.FIRE_RESISTANCE);
                player.getPersistentData().remove(EFFECT_TAG + MobEffects.FIRE_RESISTANCE.getRegisteredName().toString());
            }



            //Damage Boost//
            Map<Class<? extends Item>, Integer> strengthAmplifiers = new HashMap<>();
            strengthAmplifiers.put(WingsOfDoomItem.class, 4);
            strengthAmplifiers.put(GimlisAxeItem.class, 1);
            strengthAmplifiers.put(DarknessMaceItem.class, 2);
            strengthAmplifiers.put(HammerItem.class, 7);
            strengthAmplifiers.put(LanceItem.class, 1);

            int strengthAmplifier = -1;

            for (Map.Entry<Class<? extends Item>, Integer> entry : strengthAmplifiers.entrySet()) {
                if (entry.getKey().isInstance(mainHandItem.getItem()) || entry.getKey().isInstance(offHandItem.getItem())) {
                    strengthAmplifier = entry.getValue();
                    break;
                }
            }

            if (strengthAmplifier >= 0) {
                MobEffectInstance currentEffect = player.getEffect(MobEffects.DAMAGE_BOOST);
                if (currentEffect == null || currentEffect.getAmplifier() < strengthAmplifier) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 12000, strengthAmplifier, false, false, true));
                    player.getPersistentData().putBoolean(EFFECT_TAG + MobEffects.DAMAGE_BOOST.getRegisteredName().toString(), true);
                }
            } else if (player.getPersistentData().getBoolean(EFFECT_TAG + MobEffects.DAMAGE_BOOST.getRegisteredName().toString())) {
                player.removeEffect(MobEffects.DAMAGE_BOOST);
                player.getPersistentData().remove(EFFECT_TAG + MobEffects.DAMAGE_BOOST.getRegisteredName().toString());
            }



            //Movement Speed//
            Map<Class<? extends Item>, Integer> speedAmplifiers = new HashMap<>();
            speedAmplifiers.put(WingsOfDoomItem.class, 2);
            speedAmplifiers.put(LanceItem.class, 1);
            speedAmplifiers.put(SwiftnessDaggerItem.class, 5);

            int SpeedAmplifier = -1;

            for (Map.Entry<Class<? extends Item>, Integer> entry : speedAmplifiers.entrySet()) {
                if (entry.getKey().isInstance(mainHandItem.getItem()) || entry.getKey().isInstance(offHandItem.getItem())) {
                    SpeedAmplifier = entry.getValue();
                    break;
                }
            }

            if (SpeedAmplifier >= 0) {
                MobEffectInstance currentEffect = player.getEffect(MobEffects.MOVEMENT_SPEED);
                if (currentEffect == null || currentEffect.getAmplifier() < SpeedAmplifier) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 12000, SpeedAmplifier, false, false, true));
                    player.getPersistentData().putBoolean(EFFECT_TAG + MobEffects.MOVEMENT_SPEED.getRegisteredName().toString(), true);
                }
            } else if (player.getPersistentData().getBoolean(EFFECT_TAG + MobEffects.MOVEMENT_SPEED.getRegisteredName().toString())) {
                player.removeEffect(MobEffects.MOVEMENT_SPEED);
                player.getPersistentData().remove(EFFECT_TAG + MobEffects.MOVEMENT_SPEED.getRegisteredName().toString());
            }


            //DAMAGE_RESISTANCE//
            Map<Class<? extends Item>, Integer> resAmplifiers = new HashMap<>();
            resAmplifiers.put(WingsOfDoomItem.class, 3);
            resAmplifiers.put(GimlisAxeItem.class, 2);
            resAmplifiers.put(DarknessMaceItem.class, 2);
            resAmplifiers.put(WitherSwordItem.class, 0);

            int resAmplifier = -1;

            for (Map.Entry<Class<? extends Item>, Integer> entry : resAmplifiers.entrySet()) {
                if (entry.getKey().isInstance(mainHandItem.getItem()) || entry.getKey().isInstance(offHandItem.getItem())) {
                    resAmplifier = entry.getValue();
                    break;
                }
            }

            if (resAmplifier >= 0) {
                MobEffectInstance currentEffect = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
                if (currentEffect == null || currentEffect.getAmplifier() < resAmplifier) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12000, resAmplifier, false, false, true));
                    player.getPersistentData().putBoolean(EFFECT_TAG + MobEffects.DAMAGE_RESISTANCE.getRegisteredName().toString(), true);
                }
            } else if (player.getPersistentData().getBoolean(EFFECT_TAG + MobEffects.DAMAGE_RESISTANCE.getRegisteredName().toString())) {
                player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                player.getPersistentData().remove(EFFECT_TAG + MobEffects.DAMAGE_RESISTANCE.getRegisteredName().toString());
            }


            //SATURATION//
            Map<Class<? extends Item>, Integer> satAmplifiers = new HashMap<>();
            satAmplifiers.put(WingsOfDoomItem.class, 1);

            int satAmplifier = -1;

            for (Map.Entry<Class<? extends Item>, Integer> entry : satAmplifiers.entrySet()) {
                if (entry.getKey().isInstance(mainHandItem.getItem()) || entry.getKey().isInstance(offHandItem.getItem())) {
                    satAmplifier = entry.getValue();
                    break;
                }
            }

            if (satAmplifier >= 0) {
                MobEffectInstance currentEffect = player.getEffect(MobEffects.SATURATION);
                if (currentEffect == null || currentEffect.getAmplifier() < satAmplifier) {
                    player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 12000, satAmplifier, false, false, true));
                    player.getPersistentData().putBoolean(EFFECT_TAG + MobEffects.SATURATION.getRegisteredName().toString(), true);
                }
            } else if (player.getPersistentData().getBoolean(EFFECT_TAG + MobEffects.SATURATION.getRegisteredName().toString())) {
                player.removeEffect(MobEffects.SATURATION);
                player.getPersistentData().remove(EFFECT_TAG + MobEffects.SATURATION.getRegisteredName().toString());
            }


            //NIGHT_VISION//
            Map<Class<? extends Item>, Integer> nightAmplifiers = new HashMap<>();
            nightAmplifiers.put(WingsOfDoomItem.class, 0);

            int nightAmplifier = -1;

            for (Map.Entry<Class<? extends Item>, Integer> entry : nightAmplifiers.entrySet()) {
                if (entry.getKey().isInstance(mainHandItem.getItem()) || entry.getKey().isInstance(offHandItem.getItem())) {
                    nightAmplifier = entry.getValue();
                    break;
                }
            }

            if (nightAmplifier >= 0) {
                MobEffectInstance currentEffect = player.getEffect(MobEffects.NIGHT_VISION);
                if (currentEffect == null || currentEffect.getAmplifier() < nightAmplifier) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 12000, nightAmplifier, false, false, true));
                    player.getPersistentData().putBoolean(EFFECT_TAG + MobEffects.NIGHT_VISION.getRegisteredName().toString(), true);
                }
            } else if (player.getPersistentData().getBoolean(EFFECT_TAG + MobEffects.NIGHT_VISION.getRegisteredName().toString())) {
                player.removeEffect(MobEffects.NIGHT_VISION);
                player.getPersistentData().remove(EFFECT_TAG + MobEffects.NIGHT_VISION.getRegisteredName().toString());
            }

            //REGENERATION//
            Map<Class<? extends Item>, Integer> regenAmplifiers = new HashMap<>();
            regenAmplifiers.put(WingsOfDoomItem.class, 1);

            int regenAmplifier = -1;

            for (Map.Entry<Class<? extends Item>, Integer> entry : regenAmplifiers.entrySet()) {
                if (entry.getKey().isInstance(mainHandItem.getItem()) || entry.getKey().isInstance(offHandItem.getItem())) {
                    regenAmplifier = entry.getValue();
                    break;
                }
            }

            if (regenAmplifier >= 0) {
                MobEffectInstance currentEffect = player.getEffect(MobEffects.REGENERATION);
                if (currentEffect == null || currentEffect.getAmplifier() < regenAmplifier) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 12000, regenAmplifier, false, false, true));
                    player.getPersistentData().putBoolean(EFFECT_TAG + MobEffects.REGENERATION.getRegisteredName().toString(), true);
                }
            } else if (player.getPersistentData().getBoolean(EFFECT_TAG + MobEffects.REGENERATION.getRegisteredName().toString())) {
                player.removeEffect(MobEffects.REGENERATION);
                player.getPersistentData().remove(EFFECT_TAG + MobEffects.REGENERATION.getRegisteredName().toString());
            }










            //Immunities//

            //Blindness//
            if (hasBlindnessDaggerItem || hasWingsofDoomItem) {
                if (player.hasEffect(MobEffects.BLINDNESS)) {
                    player.removeEffect(MobEffects.BLINDNESS);
                }
            }

            //Darkness//
            if (hasDarknessMaceItem || hasWingsofDoomItem) {
                if (player.hasEffect(MobEffects.DARKNESS)) {
                    player.removeEffect(MobEffects.DARKNESS);
                }
            }

            //Poison//
            if (hasPoisonSwordItem || hasWingsofDoomItem) {
                if (player.hasEffect(MobEffects.POISON)) {
                    player.removeEffect(MobEffects.POISON);
                }
            }

            //Movement Slowdown//
            if (hasLanceItem || hasWingsofDoomItem || hasSwiftnessDaggerItem || hasIceSwordItem) {
                if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                }
            }

            //Weakness//
            if (hasWingsofDoomItem) {
                if (player.hasEffect(MobEffects.WEAKNESS)) {
                    player.removeEffect(MobEffects.WEAKNESS);
                }
            }

            //Wither//
            if (hasWitherSwordItem || hasWingsofDoomItem) {
                if (player.hasEffect(MobEffects.WITHER)) {
                    player.removeEffect(MobEffects.WITHER);
                }
            }
    }
}
