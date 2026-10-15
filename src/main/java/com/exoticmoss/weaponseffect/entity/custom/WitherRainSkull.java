package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/// The skulls launched by the "Wither Rain" ability of the Wither Sword.
/// They look like the Wither's skulls, but the damage is controlled here (not the Wither Boss attack).
public class WitherRainSkull extends WitherSkull {
    /// Damage done to the mob hit by the skull.
    public static final float DAMAGE = 8.0F;
    /// Wither effect given to the mob hit (seconds and level: 0 = Wither I, 1 = Wither II...). 0 seconds = no effect.
    public static final int WITHER_SECONDS = 10;
    public static final int WITHER_AMPLIFIER = 1;
    /// Power of the explosion when the skull hits something (1.0 = like the Wither's skulls).
    /// 0 = no explosion, only the visual effect and sound. The explosion also hurts mobs around it.
    public static final float EXPLOSION_POWER = 0.5F;
    /// If the explosion breaks blocks.
    public static final boolean EXPLOSION_BREAKS_BLOCKS = false;

    public WitherRainSkull(EntityType<? extends WitherRainSkull> type, Level level) {
        super(type, level);
    }

    public WitherRainSkull(Level level, LivingEntity owner) {
        this(ModEntities.WITHER_RAIN_SKULL.get(), level);
        this.setOwner(owner);
    }

    // The skulls never hit the player who launched them
    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity != this.getOwner() && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Entity target = hitResult.getEntity();
        Entity owner = this.getOwner();
        DamageSource source = owner instanceof LivingEntity livingOwner
                ? this.damageSources().witherSkull(this, livingOwner)
                : this.damageSources().magic();

        boolean wasHurt = target.hurtServer(serverLevel, source, DAMAGE);
        if (wasHurt && WITHER_SECONDS > 0 && target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * WITHER_SECONDS, WITHER_AMPLIFIER), this.getEffectSource());
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) hitResult);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) hitResult);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            if (EXPLOSION_POWER > 0) {
                serverLevel.explode(this, this.getX(), this.getY(), this.getZ(), EXPLOSION_POWER, false,
                        EXPLOSION_BREAKS_BLOCKS ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
            } else {
                // Only the visual effect, no extra damage
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.4F, 1.4F);
            }
            this.discard();
        }
    }
}
