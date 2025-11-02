package com.vector.weaponseffect.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;


public class WitherSwordItem extends SwordItem {
    private static final int COOLDOWN_TICKS = 40;
    private static final int NUM_HEADS = 10;

    public WitherSwordItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        boolean result = super.hurtEnemy(pStack, pTarget, pAttacker);
        if (result) {
            pTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 6), pAttacker);
        }
        return result;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @NotNull TooltipContext pContext, List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("wither.sword.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("wither.line.2"));
        pTooltipComponents.add(Component.translatable("wither.immunities").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("wither.immunities.tooltip").withStyle(ChatFormatting.DARK_GRAY));
        pTooltipComponents.add(Component.translatable("wither.line.3"));
        pTooltipComponents.add(Component.translatable("wither.ability").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("wither.ability.name").withStyle(ChatFormatting.DARK_GRAY));
        pTooltipComponents.add(Component.translatable("wither.line.4"));
        pTooltipComponents.add(Component.translatable("wither.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (!pLevel.isClientSide) {
            if (pPlayer.getCooldowns().isOnCooldown(this)) {
                pPlayer.displayClientMessage(Component.translatable("item.wither_sword.cooldown"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            pPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            launchWitherSkulls(pPlayer, pLevel, NUM_HEADS);
        }

        if (pLevel.isClientSide) {
            handleWitherSound(pLevel, pPlayer.blockPosition());
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
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
        handleWitherSound(level, attacker.blockPosition());
    }

    private void handleWitherSound(Level level, BlockPos position) {
        if (level.isClientSide()) {
            // Este metodo, sendo chamado aqui, será executado apenas no cliente
            // E o metodo playClientWitherSound está anotado para ser removido no servidor
            playClientWitherSound(level, position);
        } else {
            // Este código é executado no servidor e notifica todos os clientes próximos
            level.playSound(null, position, SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playClientWitherSound(Level level, BlockPos position) {
        if (level.isClientSide) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                SoundEvent soundEvent = SoundEvents.LAVA_EXTINGUISH;
                float volume = 0.5F;
                float pitch = 1.0F;

                SimpleSoundInstance soundInstance = new SimpleSoundInstance(
                        soundEvent,
                        SoundSource.AMBIENT,
                        volume,
                        pitch,
                        minecraft.player.getRandom(),
                        position.getX() + 0.5,
                        position.getY() + 0.5,
                        position.getZ() + 0.5
                );

                minecraft.getSoundManager().play(soundInstance);
            }
        }
    }
}
