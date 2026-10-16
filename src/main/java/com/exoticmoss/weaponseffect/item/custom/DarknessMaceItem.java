package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.level.AbilityWeapon;
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

public class DarknessMaceItem extends Item implements AbilityWeapon {
    public DarknessMaceItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 20), pAttacker);
        pTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 2), pAttacker);
        super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltip, TooltipFlag pTooltipFlag) {
        pTooltip.accept(Component.translatable("darkness.mace.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("darkness.line.2"));
        pTooltip.accept(Component.translatable("darkness.immunities").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("darkness.immunities.tooltip").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }
}
