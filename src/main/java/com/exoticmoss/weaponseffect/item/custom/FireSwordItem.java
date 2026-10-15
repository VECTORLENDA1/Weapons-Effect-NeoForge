package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.ability.FlameWave;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.function.Consumer;

public class FireSwordItem extends Item {
    private static final int COOLDOWN_TICKS = 40;

    public FireSwordItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.fireImmune()) {
            target.setRemainingFireTicks(15 * 20);
            playFlamethrowerSound(target.level(), target.blockPosition());

            spawnParticles(target.level(), target.position(), 20, target.getBbWidth() * 2.5, target.getBbHeight());
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltip, TooltipFlag pTooltipFlag) {
        pTooltip.accept(Component.translatable("fire.sword.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("fire.line.2"));
        pTooltip.accept(Component.translatable("fire.immunities").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("fire.immunities.tooltip").withStyle(ChatFormatting.RED));
        pTooltip.accept(Component.translatable("fire.line.3"));
        pTooltip.accept(Component.translatable("fire.ability").withStyle(ChatFormatting.WHITE));
        pTooltip.accept(Component.translatable("fire.ability.name").withStyle(ChatFormatting.RED));
        pTooltip.accept(Component.translatable("fire.line.4"));
        pTooltip.accept(Component.translatable("fire.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltip, pTooltipFlag);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(stack)) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("item.fire.sword.cooldown"));
            }
            return InteractionResult.FAIL;
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        // A wave of flames (see FlameWave): done by the server and sent to every player nearby
        if (level instanceof ServerLevel serverLevel) {
            FlameWave.launch(serverLevel, player);
        }

        return InteractionResult.SUCCESS;
    }

    private void spawnParticles(Level level, Vec3 position, int particleCount, double width, double height) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Random rand = new Random();

        for (int i = 0; i < particleCount; i++) {
            double offsetX = (rand.nextDouble() - 0.5) * width;
            double offsetY = rand.nextDouble() * height;
            double offsetZ = (rand.nextDouble() - 0.5) * width;

            serverLevel.sendParticles(ParticleTypes.FLAME, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    // Played by the server: every player nearby hears it
    private void playFlamethrowerSound(Level level, BlockPos position) {
        if (!level.isClientSide()) {
            level.playSound(null, position, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 4.0F, 1.0F);
        }
    }
}
