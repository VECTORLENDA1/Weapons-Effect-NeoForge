package com.exoticmoss.weaponseffect.item.custom;

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
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.function.Consumer;


public class WitherSwordItem extends Item {
    private static final int COOLDOWN_TICKS = 40;
    private static final int NUM_HEADS = 10;

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

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (pPlayer.getCooldowns().isOnCooldown(itemstack)) {
            if (!pLevel.isClientSide()) {
                pPlayer.sendOverlayMessage(Component.translatable("item.wither_sword.cooldown"));
            }
            return InteractionResult.FAIL;
        }

        pPlayer.getCooldowns().addCooldown(itemstack, COOLDOWN_TICKS);

        if (!pLevel.isClientSide()) {
            launchWitherSkulls(pPlayer, pLevel, NUM_HEADS);
        }

        return InteractionResult.SUCCESS;
    }

    private void launchWitherSkulls(LivingEntity attacker, Level level, int numHeads) {
        Vec3 startVec = attacker.position().add(0, attacker.getEyeHeight() * 0.5, 0);
        Vec3 forwardVec = attacker.getLookAngle().normalize();
        Random rand = new Random();

        for (int i = 0; i < numHeads; i++) {
            WitherSkull witherSkull = new WitherSkull(level, attacker, forwardVec);
            witherSkull.setPos(startVec.x, startVec.y - 0.1, startVec.z);

            Vec3 randomOffset = new Vec3(rand.nextDouble() * 0.5 - 0.25, 0, rand.nextDouble() * 0.5 - 0.25);
            witherSkull.setDeltaMovement(forwardVec.add(randomOffset).normalize().scale(0.5));

            level.addFreshEntity(witherSkull);
        }
        playWitherSound(level, attacker.blockPosition());
    }

    // Played by the server: every player nearby hears it
    private void playWitherSound(Level level, BlockPos position) {
        level.playSound(null, position, SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.0F);
    }
}
