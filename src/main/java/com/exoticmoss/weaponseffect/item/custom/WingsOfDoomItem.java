package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.exoticmoss.weaponseffect.entity.BlackHoleEntity;

import java.util.function.Consumer;


public class WingsOfDoomItem extends Item implements AbilityWeapon {
    /// Cooldown of the black hole ability (in ticks, 20 = 1 second)
    private static final int COOLDOWN_TICKS = 300;

    public WingsOfDoomItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 4), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 4), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 4), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 4), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 5), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 3), attacker);

        if (target.level() instanceof ServerLevel serverLevel) {
            if (!target.fireImmune()) {
                target.igniteForSeconds(20);
            } else {
                // Can't burn: show some flames instead
                serverLevel.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                        20, target.getBbWidth() / 2, target.getBbHeight() / 2, target.getBbWidth() / 2, 0.02);
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltip, TooltipFlag pTooltipFlag) {
        pTooltip.accept(Component.translatable("wings.of.doom.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wings.of.doom.tooltip.2").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wings.of.doom.tooltip.3").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wings.of.doom.line.2"));
        pTooltip.accept(Component.translatable("wings.of.doom.immunities").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wings.of.doom.immunities.tooltip").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("wings.of.doom.immunities.tooltip.2").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("wings.of.doom.immunities.tooltip.3").withStyle(ChatFormatting.GREEN));
        pTooltip.accept(Component.translatable("wings.of.doom.immunities.tooltip.4").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wings.of.doom.immunities.tooltip.5").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wings.of.doom.immunities.tooltip.6").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("wings.of.doom.immunities.tooltip.7").withStyle(ChatFormatting.RED));
        pTooltip.accept(Component.translatable("wings.of.doom.line.3"));
        pTooltip.accept(Component.translatable("wings.of.doom.abilitys").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("wings.of.doom.abilitys.name").withStyle(ChatFormatting.DARK_GRAY));
        pTooltip.accept(Component.translatable("wings.of.doom.line.4"));
        pTooltip.accept(Component.translatable("wings.of.doom.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }

    // Right-click: creates a Black Hole where the player is looking
    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (!world.isClientSide()) {
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePosition = player.getEyePosition(1.0f);

            HitResult hitResult = player.pick(20.0, 1.0f, false);
            Vec3 spawnPosition;

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                spawnPosition = new Vec3(blockHit.getBlockPos().getX() + 0.5,
                        blockHit.getBlockPos().getY() + 0.5,
                        blockHit.getBlockPos().getZ() + 0.5);
            } else {
                spawnPosition = eyePosition.add(lookVec.scale(5.0));
            }


            int lifetime = BlackHoleEntity.DEFAULT_LIFETIME;
            float maxRadius = BlackHoleEntity.DEFAULT_MAX_RADIUS;

            BlackHoleEntity blackHole = new BlackHoleEntity(
                    world,
                    spawnPosition.x, spawnPosition.y, spawnPosition.z,
                    lifetime,
                    maxRadius,
                    player.getUUID()
            );

            world.addFreshEntity(blackHole);

            //Cooldown of the black hole ability
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        }
        return InteractionResult.SUCCESS;
    }
}
