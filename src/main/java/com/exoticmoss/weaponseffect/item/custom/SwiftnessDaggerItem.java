package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.entity.custom.DashEntity;
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

public class SwiftnessDaggerItem extends Item implements AbilityWeapon {
    /// Time between two uses of the ability for each level { I, II, III, IV, V } (ticks, 20 = 1 second).
    private static final int[] COOLDOWN_TICKS = {60, 50, 40, 30, 0};

    public SwiftnessDaggerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 4), pAttacker);
        super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltip, TooltipFlag pTooltipFlag) {
        pTooltip.accept(Component.translatable("swiftness.dagger.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("swiftness.line.2"));
        pTooltip.accept(Component.translatable("swiftness.immunities").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("swiftness.immunities.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("swiftness.line.3"));
        pTooltip.accept(Component.translatable("swiftness.ability").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("swiftness.ability.name").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("swiftness.line.4"));
        pTooltip.accept(Component.translatable("swiftness.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }

    // Right-click: the ability (a dash to where you aim, see DashEntity)
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return Abilities.use(level, player, hand, COOLDOWN_TICKS, "item.swiftness.dagger.cooldown",
                (server, user, weapon, weaponLevel) -> DashEntity.launch(server, user, weaponLevel));
    }
}
