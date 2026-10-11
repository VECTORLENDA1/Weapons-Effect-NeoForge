package com.exoticmoss.weaponseffect.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

import java.util.List;
import java.util.Properties;

public class BlindnessDaggerItem extends Item {
    public BlindnessDaggerItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200,5), pAttacker);
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("blindness.dagger.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("blindness.line.2"));
        pTooltipComponents.add(Component.translatable("blindness.immunities").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("blindness.immunities.tooltip").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
