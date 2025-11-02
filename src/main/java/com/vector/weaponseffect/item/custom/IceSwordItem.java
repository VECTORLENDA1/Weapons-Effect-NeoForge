package com.vector.weaponseffect.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.WeakHashMap;

public class IceSwordItem extends SwordItem {
    private static final WeakHashMap<LivingEntity, Integer> freezingEntities = new WeakHashMap<>();

    public IceSwordItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (!pTarget.level().isClientSide()) {
            pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 100), pAttacker);
            applyFreezingEffect(pTarget);
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
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
                        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 100));
                    }
                    entry.setValue(ticksLeft - 1);
                    return ticksLeft <= 0;
                }
                return true;
            });

    }


    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("ice.sword.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("ice.line.2"));
        pTooltipComponents.add(Component.translatable("ice.immunities").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("ice.immunities.tooltip").withStyle(ChatFormatting.BLUE));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
