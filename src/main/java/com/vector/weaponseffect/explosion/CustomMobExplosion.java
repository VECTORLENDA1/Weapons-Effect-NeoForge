package com.vector.weaponseffect.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class CustomMobExplosion extends Explosion {
    private final Random random = new Random();
    private final Level level;

    public CustomMobExplosion(Level level, @Nullable Entity source, double x, double y, double z, float radius,
                              List<BlockPos> toBlow, BlockInteraction blockInteraction,
                              ParticleOptions smallExplosionParticles,
                              ParticleOptions largeExplosionParticles, Holder.Reference<SoundEvent> genericExplode) {
        super(level, source, x, y, z, radius, toBlow, blockInteraction, smallExplosionParticles,
                largeExplosionParticles, SoundEvents.GENERIC_EXPLODE);
        this.level = level;
    }

    @Override
    public void explode() {
        super.explode();
        if (level.isClientSide) {
            return;
        }
        applyDamageToEntities();
        playExplosionSound();
        spawnParticlesServerSide();
    }

    private void applyDamageToEntities() {
        float radius = getExplosionRadius();
        AABB aabb = new AABB(getX() - radius, getY() - radius, getZ() - radius,
                getX() + radius, getY() + radius, getZ() + radius);

        List<Entity> entities = this.level.getEntities(null, aabb);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && entity != getExploder()) {
                if (getExploder() instanceof Player player) {
                    DamageSource damageSource = player.damageSources().playerAttack(player);
                    livingEntity.hurt(damageSource, 50.0F);
                }
            }
        }
    }

    private void playExplosionSound() {
        this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.3F,
                (2.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);
    }

    private void spawnParticlesServerSide() {
        if (!level.isClientSide) {
            float radius = getExplosionRadius();
            for (int i = 0; i < 100; i++) {
                double xOffset = (level.random.nextDouble() - 0.5) * 2.0 * radius;
                double zOffset = (level.random.nextDouble() - 0.5) * 2.0 * radius;
                double x = getX() + xOffset;
                double y = getY();
                double z = getZ() + zOffset;

                ((ServerLevel) level).sendParticles(ParticleTypes.GUST, x, y, z, 1, 0.0, 0.0, 0.0, 0.02);
            }
        }
    }

    private float getExplosionRadius() {
        return super.radius();
    }

    private double getX() {
        return super.center().x;
    }

    private double getY() {
        return super.center().y;
    }

    private double getZ() {
        return super.center().z;
    }

    private Entity getExploder() {
        return super.getDirectSourceEntity();
    }
}





