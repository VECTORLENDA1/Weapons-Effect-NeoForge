package com.vector.weaponseffect.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.vector.weaponseffect.explosion.CustomMobExplosion;

import java.util.List;
import java.util.Random;

public class HammerItem extends SwordItem {
    private static final int COOLDOWN_TICKS = 50;

    public HammerItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3), pAttacker);
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("hammer.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("hammer.line.2"));
        pTooltipComponents.add(Component.translatable("hammer.ability").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("hammer.ability.name").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("hammer.line.3"));
        pTooltipComponents.add(Component.translatable("hammer.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        Level level = context.getLevel();

        if (player != null) {
            if (player.getCooldowns().isOnCooldown(this)) {
                player.displayClientMessage(Component.translatable("item.hammer.cooldown"), true);
                return InteractionResult.FAIL;
            }


            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            if (!level.isClientSide) {
                BlockPos pos = context.getClickedPos();
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 1.5;
                double z = pos.getZ() + 0.5;
                float radius = 2.5F;

                CustomMobExplosion explosion = new CustomMobExplosion(
                        level,
                        player,
                        x,
                        y,
                        z,
                        radius,
                        List.of(),
                        Explosion.BlockInteraction.KEEP,
                        ParticleTypes.EXPLOSION,
                        ParticleTypes.EXPLOSION,
                        SoundEvents.GENERIC_EXPLODE
                );

                explosion.explode();
            } else {
                spawnParticles(level, context.getClickedPos(), player);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void spawnParticles(Level level, BlockPos clickedPos, Player player) {
        double maxReachDistance = 5.0D;

        Vec3 playerLookVec = player.getViewVector(1.0F);
        Vec3 playerPos = player.getEyePosition();
        Vec3 endPos = playerPos.add(playerLookVec.scale(maxReachDistance));

        BlockHitResult hitResult = level.clip(new ClipContext(playerPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            int range = 5;
            double fixedY = blockPos.getY() + 1.5D;

            Random rand = new Random();
            for (int x = -range; x <= range; ++x) {
                for (int z = -range; z <= range; ++z) {
                    double particleX = blockPos.getX() + 0.5D + x + (rand.nextDouble() - 0.5D);
                    double particleZ = blockPos.getZ() + 0.5D + z + (rand.nextDouble() - 0.5D);

                    level.addParticle(ParticleTypes.GUST, particleX, fixedY, particleZ, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }
}
