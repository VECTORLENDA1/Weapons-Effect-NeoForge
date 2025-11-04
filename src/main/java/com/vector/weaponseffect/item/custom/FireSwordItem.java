package com.vector.weaponseffect.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class FireSwordItem extends SwordItem {
    private static final int COOLDOWN_TICKS = 40;

    public FireSwordItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (result && !target.fireImmune()) {
            target.setRemainingFireTicks(15 * 20);
            handleFlamethrowerSound(target.level(), target.blockPosition());

            spawnParticles(target.level(), target.position(), 20, target.getBbWidth() * 2.5, target.getBbHeight());
        }
        return result;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @NotNull TooltipContext pContext, List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("fire.sword.tooltip").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("fire.line.2"));
        pTooltipComponents.add(Component.translatable("fire.immunities").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("fire.immunities.tooltip").withStyle(ChatFormatting.RED));
        pTooltipComponents.add(Component.translatable("fire.line.3"));
        pTooltipComponents.add(Component.translatable("fire.ability").withStyle(ChatFormatting.WHITE));
        pTooltipComponents.add(Component.translatable("fire.ability.name").withStyle(ChatFormatting.RED));
        pTooltipComponents.add(Component.translatable("fire.line.4"));
        pTooltipComponents.add(Component.translatable("fire.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            player.displayClientMessage(Component.translatable("item.fire.sword.cooldown"), true);
            return InteractionResultHolder.fail(stack);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        launchFlamethrower(player, level, 3.0, 7.0);
        spawnParticles(level, player, 150, 3.0, 7.0);
        handleFlamethrowerSound(level, player.blockPosition());

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void launchFlamethrower(LivingEntity attacker, Level level, double width, double length) {
        Vec3 startVec = attacker.position().add(0, attacker.getEyeHeight() * 0.5, 0);
        Vec3 forwardVec = attacker.getForward().normalize();
        Vec3 rightVec = new Vec3(-forwardVec.z, 0, forwardVec.x).normalize();

        for (double z = 0; z < length; z += 0.5) {
            Vec3 currentPos = startVec.add(forwardVec.scale(z));

            for (double x = -width / 2; x <= width / 2; x += 0.5) {
                Vec3 impactPos = currentPos.add(rightVec.scale(x));

                AABB aabb = new AABB(
                        impactPos.x - 0.25, impactPos.y - 0.5, impactPos.z - 0.25,
                        impactPos.x + 0.25, impactPos.y + 0.5, impactPos.z + 0.25
                );

                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);

                for (LivingEntity entity : entities) {
                    if (entity != attacker && !entity.fireImmune()) {
                        entity.setRemainingFireTicks(5 * 20);
                        applyDamageToEntity(attacker, entity, 13.0F);
                    }
                }
            }
        }
        handleFlamethrowerSound(level, attacker.blockPosition());
    }

    private void applyDamageToEntity(LivingEntity attacker, LivingEntity target, float damage) {
        DamageSource damageSource;

        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            damageSource = player.damageSources().playerAttack(player);
        } else {
            damageSource = attacker.damageSources().mobAttack(attacker);
        }

        target.hurt(damageSource, damage);
    }

    private void spawnParticles(Level level, Player player, int particleCount, double width, double length) {
        Vec3 playerPos = player.position().add(0, player.getEyeHeight() * 0.8, 0);
        Vec3 forwardVec = player.getForward().normalize();
        Vec3 rightVec = new Vec3(-forwardVec.z, 0, forwardVec.x).normalize();
        Random rand = new Random();

        for (int i = 0; i < particleCount; i++) {
            double offsetX = (rand.nextDouble() - 0.5) * width;
            double offsetZ = rand.nextDouble() * length;
            Vec3 particlePos = playerPos
                    .add(rightVec.scale(offsetX))
                    .add(forwardVec.scale(offsetZ));

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void spawnParticles(Level level, Vec3 position, int particleCount, double width, double height) {
        Random rand = new Random();

        for (int i = 0; i < particleCount; i++) {
            double offsetX = (rand.nextDouble() - 0.5) * width;
            double offsetY = rand.nextDouble() * height;
            double offsetZ = (rand.nextDouble() - 0.5) * width;

            double x = position.x + offsetX;
            double y = position.y + offsetY;
            double z = position.z + offsetZ;

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void handleFlamethrowerSound(Level level, BlockPos position) {
        if (level.isClientSide()) {
            // Este metodo, sendo chamado aqui, será executado apenas no cliente
            // E o metodo playClientFlamethrowerSound está anotado para ser removido no servidor
            playClientFlamethrowerSound(level, position);
        } else {
            // Este código é executado no servidor e notifica todos os clientes próximos
            level.playSound(null, position, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 4.0F, 1.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playClientFlamethrowerSound(Level level, BlockPos position) {
        if (level.isClientSide) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                SoundEvent soundEvent = SoundEvents.FIRE_AMBIENT;
                float volume = 4.0F;
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
