package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.level.Abilities;
import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import com.exoticmoss.weaponseffect.entity.custom.WitherRainSkull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.function.Consumer;


public class WitherSwordItem extends Item implements AbilityWeapon {
    /// Time between two uses of the ability for each level { I, II, III, IV, V } (ticks, 20 = 1 second).
    private static final int[] COOLDOWN_TICKS = {40, 35, 30, 25, 0};

    public WitherSwordItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 6), pAttacker);
        super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltip, TooltipFlag pTooltipFlag) {
        pTooltip.accept(Component.translatable("wither.sword.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wither.line.2"));
        pTooltip.accept(Component.translatable("wither.immunities").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wither.immunities.tooltip").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("wither.line.3"));
        pTooltip.accept(Component.translatable("wither.ability").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wither.ability.name").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("wither.line.4"));
        pTooltip.accept(Component.translatable("wither.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }

    // Right-click: the ability (a rain of wither skulls, see WitherRainSkull)
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return Abilities.use(level, player, hand, COOLDOWN_TICKS, "item.wither_sword.cooldown",
                (server, user, weapon, weaponLevel) -> WitherRainSkull.launch(server, user, weaponLevel));
    }
}
