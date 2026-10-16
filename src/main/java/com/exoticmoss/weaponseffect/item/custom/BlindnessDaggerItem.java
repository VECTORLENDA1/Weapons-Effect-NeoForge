package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.entity.custom.SmokeBombEntity;
import com.exoticmoss.weaponseffect.level.Abilities;
import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
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

public class BlindnessDaggerItem extends Item implements AbilityWeapon {
    /// Time between two uses of the ability for each level { I, II, III, IV, V } (ticks, 20 = 1 second).
    public static final int[] COOLDOWN_TICKS = {300, 260, 220, 180, 0};

    public BlindnessDaggerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        WeaponLevels.rewardNormalHit(pStack, pAttacker);
        pTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 5), pAttacker);
        super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltip, TooltipFlag pTooltipFlag) {
        pTooltip.accept(Component.translatable("blindness.dagger.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("blindness.line.2"));
        pTooltip.accept(Component.translatable("blindness.immunities").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("blindness.immunities.tooltip").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("blindness.line.3"));
        pTooltip.accept(Component.translatable("blindness.ability").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("blindness.ability.name").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("blindness.line.4"));
        pTooltip.accept(Component.translatable("blindness.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }

    // Right-click: the ability (a smoke bomb, see SmokeBombEntity)
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return Abilities.use(level, player, hand, COOLDOWN_TICKS, "item.blindness.dagger.cooldown",
                (server, user, weapon, weaponLevel) -> SmokeBombEntity.launch(server, user, weaponLevel));
    }
}
