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

import java.util.function.Consumer;

public class PoisonSwordItem extends Item {
    public PoisonSwordItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 100), pAttacker);
        super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltip, TooltipFlag pTooltipFlag) {
        pTooltip.accept(Component.translatable("poison.sword.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("poison.line.2"));
        pTooltip.accept(Component.translatable("poison.immunities").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("poison.immunities.tooltip").withStyle(ChatFormatting.GREEN));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }
}
