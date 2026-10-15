package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.custom.WitherSwordItem;
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
import net.minecraft.world.phys.AABB;

/// The skulls launched by the "Wither Rain" ability of the Wither Sword.
/// They look like the Wither's skulls, but the damage is controlled here (not the Wither Boss attack).
public class WitherRainSkull extends WitherSkull {
    /// How many skulls the Wither Sword launches each time.
    public static final int SKULL_COUNT = 10;
    /// Damage done to the mob hit by the skull.
    public static final float DAMAGE = 8.0F;
    /// Wither effect given to the mob hit (seconds and level: 0 = Wither I, 1 = Wither II...). 0 seconds = no effect.
    public static final int WITHER_SECONDS = 10;
    public static final int WITHER_AMPLIFIER = 1;
    /// The explosion when the skull hits something. It only does damage: it doesn't break blocks,
    /// doesn't push anything and doesn't touch dropped items. It never hurts the player who launched
    /// the skulls, nor anyone holding a Wither Sword (in the right or left hand).
    /// Radius in blocks (0 = no explosion damage, only the visual effect and sound).
    public static final float EXPLOSION_RADIUS = 2.0F;
    /// Damage in the center of the explosion; it gets smaller further away (0 at the edge of the radius).
    public static final float EXPLOSION_DAMAGE = 6.0F;

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
            Entity hitEntity = hitResult instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
            explode(serverLevel, hitEntity);
            this.discard();
        }
    }

    /// Damage-only explosion: hurts the mobs around, nothing else.
    private void explode(ServerLevel level, Entity hitEntity) {
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.4F, 1.4F);
        if (EXPLOSION_RADIUS <= 0 || EXPLOSION_DAMAGE <= 0) return;

        Entity owner = this.getOwner();
        DamageSource source = owner instanceof LivingEntity livingOwner
                ? this.damageSources().witherSkull(this, livingOwner)
                : this.damageSources().magic();

        AABB area = this.getBoundingBox().inflate(EXPLOSION_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            // The mob hit directly already took the skull's damage
            if (target == owner || target == hitEntity || isHoldingWitherSword(target)) continue;

            double distance = target.position().distanceTo(this.position());
            if (distance > EXPLOSION_RADIUS) continue;
            float damage = (float) (EXPLOSION_DAMAGE * (1.0 - distance / EXPLOSION_RADIUS));
            if (damage > 0) target.hurtServer(level, source, damage);
        }
    }

    private static boolean isHoldingWitherSword(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof WitherSwordItem
                || entity.getOffhandItem().getItem() instanceof WitherSwordItem;
    }
}
