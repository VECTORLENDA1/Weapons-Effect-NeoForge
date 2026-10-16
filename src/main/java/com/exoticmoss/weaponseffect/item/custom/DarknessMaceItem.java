package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.entity.custom.DarknessSlamEntity;
import com.exoticmoss.weaponseffect.level.Abilities;
import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class DarknessMaceItem extends Item implements AbilityWeapon {
    /// Time between two uses of the ability for each level { I, II, III, IV, V } (ticks, 20 = 1 second).
    private static final int[] COOLDOWN_TICKS = {200, 180, 160, 140, 0};

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
        pTooltip.accept(Component.translatable("darkness.line.3"));
        pTooltip.accept(Component.translatable("darkness.ability").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("darkness.ability.name").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("darkness.line.4"));
        pTooltip.accept(Component.translatable("darkness.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }

    // Right-click: the ability (a leap and a slam that leaves a darkness zone, see DarknessSlamEntity)
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return Abilities.use(level, player, hand, COOLDOWN_TICKS, "item.darkness.mace.cooldown",
                (server, user, weapon, weaponLevel) -> DarknessSlamEntity.leap(server, user, weaponLevel));
    }
}
