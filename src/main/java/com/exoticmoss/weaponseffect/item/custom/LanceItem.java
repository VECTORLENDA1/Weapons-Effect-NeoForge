package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import com.exoticmoss.weaponseffect.entity.custom.LanceProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/// Lance. Thrown like a trident: hold right-click to pull it back, release to throw.
/// Ability "Rain of Lances": when the thrown lance hits something, lances fall from the sky around it
/// (amount, damage and size in LanceProjectileEntity).
public class LanceItem extends Item implements AbilityWeapon {
    /// How long right-click must be held before the lance can be thrown (ticks, the same as the trident).
    private static final int CHARGE_TICKS = 10;
    /// How fast the lance is thrown (the trident is 2.5).
    private static final float THROW_SPEED = 3.0F;

    public LanceItem(Properties properties) {
        super(properties);
    }

    // Right-click: start pulling the lance back (the trident animation)
    // Normal hit: a little experience for the weapon
    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        WeaponLevels.rewardNormalHit(stack, attacker);
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    // Right-click released: throw the lance (if it was pulled back long enough)
    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingTime) {
        if (!(user instanceof Player player) || getUseDuration(stack, user) - remainingTime < CHARGE_TICKS) return false;

        if (level instanceof ServerLevel serverLevel) {
            ItemStack thrown = stack.consumeAndReturn(1, player);
            LanceProjectileEntity lance = new LanceProjectileEntity(serverLevel, player, thrown);
            lance.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, THROW_SPEED, 1.0F);
            if (player.hasInfiniteMaterials()) lance.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            serverLevel.addFreshEntity(lance);
            serverLevel.playSound(null, lance, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("lance.tooltip").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("lance.line.2"));
        tooltip.accept(Component.translatable("lance.immunities").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("lance.immunities.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("lance.line.3"));
        tooltip.accept(Component.translatable("lance.ability").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("lance.ability.name").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("lance.line.4"));
        tooltip.accept(Component.translatable("lance.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
