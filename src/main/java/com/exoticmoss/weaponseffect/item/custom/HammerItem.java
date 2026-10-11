package com.exoticmoss.weaponseffect.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Consumer;

public class HammerItem extends Item {
    private static final int COOLDOWN_TICKS = 50;
    /// Radius (in blocks) of the explosion of the ability.
    private static final float EXPLOSION_RADIUS = 2.5F;
    /// Damage done to every mob inside the explosion.
    private static final float EXPLOSION_DAMAGE = 50.0F;

    public HammerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 3), pAttacker);
        super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltip, TooltipFlag pTooltipFlag) {
        pTooltip.accept(Component.translatable("hammer.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("hammer.line.2"));
        pTooltip.accept(Component.translatable("hammer.ability").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("hammer.ability.name").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("hammer.line.3"));
        pTooltip.accept(Component.translatable("hammer.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }

    // Right-click on a block: an explosion on top of it that hurts every mob around (but doesn't break blocks)
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        Level level = context.getLevel();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("item.hammer.cooldown"));
            }
            return InteractionResult.FAIL;
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = context.getClickedPos();
            explode(serverLevel, player, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
        }
        return InteractionResult.SUCCESS;
    }

    private void explode(ServerLevel level, Player player, double x, double y, double z) {
        // Damage every mob inside the radius (not the player using the hammer)
        float radius = EXPLOSION_RADIUS;
        AABB area = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player);
        for (LivingEntity entity : entities) {
            entity.hurtServer(level, player.damageSources().playerAttack(player), EXPLOSION_DAMAGE);
        }

        // Sound and particles (sent to every player nearby)
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F,
                (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F);
        level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);

        // Ring of gusts around the explosion
        int range = 5;
        for (int dx = -range; dx <= range; ++dx) {
            for (int dz = -range; dz <= range; ++dz) {
                double particleX = x + dx + (level.getRandom().nextDouble() - 0.5D);
                double particleZ = z + dz + (level.getRandom().nextDouble() - 0.5D);
                level.sendParticles(ParticleTypes.GUST, particleX, y, particleZ, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }
}
