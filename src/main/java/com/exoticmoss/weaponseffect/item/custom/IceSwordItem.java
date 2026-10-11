package com.exoticmoss.weaponseffect.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.WeakHashMap;
import java.util.function.Consumer;

public class IceSwordItem extends Item {
    private static final WeakHashMap<LivingEntity, Integer> freezingEntities = new WeakHashMap<>();

    public IceSwordItem(Properties pProperties) {
        super(pProperties);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (!pTarget.level().isClientSide()) {
            pTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 100), pAttacker);
            applyFreezingEffect(pTarget);
        }
        super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    private void applyFreezingEffect(LivingEntity target) {
        freezingEntities.put(target, 100);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
            freezingEntities.entrySet().removeIf(entry -> {
                LivingEntity entity = entry.getKey();
                int ticksLeft = entry.getValue();

                if (entity != null && entity.isAlive()) {
                    if (ticksLeft % 20 == 0) {
                        entity.hurt(entity.damageSources().freeze(), 1.0F);
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 100));
                    }
                    entry.setValue(ticksLeft - 1);
                    return ticksLeft <= 0;
                }
                return true;
            });

    }


    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltip, TooltipFlag pTooltipFlag) {
        pTooltip.accept(Component.translatable("ice.sword.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("ice.line.2"));
        pTooltip.accept(Component.translatable("ice.immunities").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("ice.immunities.tooltip").withStyle(ChatFormatting.BLUE));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }
}
