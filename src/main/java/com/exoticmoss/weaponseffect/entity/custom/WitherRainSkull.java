package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModItems;
import com.exoticmoss.weaponseffect.item.custom.WitherSwordItem;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/// The skulls of the Wither Sword ability ("Wither Rain"). They look like the Wither's skulls,
/// but everything is controlled here (not the Wither Boss attack).
///  - Level III and up: the skulls chase the closest monster.
///  - Level V: blue skulls (the strong ones of the Wither) that give Wither III.
/// The explosion only does damage: it doesn't break blocks, doesn't push anything and doesn't touch
/// dropped items. It never hurts the player who launched the skulls, nor anyone holding a Wither Sword.
///
/// TABLES: one number for each level { I, II, III, IV, V }.
public class WitherRainSkull extends WitherSkull {
    /// How many skulls are launched each time.
    public static final int[] SKULL_COUNT = {6, 8, 10, 12, 14};
    /// Damage to the mob hit by the skull.
    public static final float[] DAMAGE = {8, 10, 12, 14, 16};
    /// Wither given to the mob hit: seconds and level (0 = Wither I, 1 = Wither II, 2 = Wither III).
    public static final int[] WITHER_SECONDS = {10, 10, 12, 12, 15};
    public static final int[] WITHER_AMPLIFIER = {1, 1, 1, 1, 2};
    /// The explosion: radius (blocks) and damage in the middle (smaller further away, 0 at the edge).
    public static final float[] EXPLOSION_RADIUS = {2, 2, 2.5F, 2.5F, 3};
    public static final float[] EXPLOSION_DAMAGE = {4, 5, 6, 7, 8};
    /// Levels where the skulls chase the closest monster, and levels with blue skulls.
    public static final boolean[] HOMING = {false, false, true, true, true};
    public static final boolean[] BLUE = {false, false, false, false, true};

    /// How far the skulls look for a monster to chase (blocks) and how fast they turn (0 to 1).
    public static final double HOMING_RANGE = 16;
    public static final double TURN_STRENGTH = 0.15;
    /// Speed of the skulls (blocks per tick).
    public static final double SPEED = 0.6;

    private int weaponLevel = 1;

    public WitherRainSkull(EntityType<? extends WitherRainSkull> type, Level level) {
        super(type, level);
    }

    private WitherRainSkull(Level level, LivingEntity owner, int weaponLevel) {
        this(ModEntities.WITHER_RAIN_SKULL.get(), level);
        this.setOwner(owner);
        this.weaponLevel = weaponLevel;
        this.setDangerous(WeaponLevels.pick(BLUE, weaponLevel));
    }

    /// The ability: a spread of skulls in front of the player.
    public static void launch(ServerLevel level, LivingEntity owner, int weaponLevel) {
        Vec3 start = owner.position().add(0, owner.getEyeHeight() * 0.5 - 0.1, 0);
        Vec3 forward = owner.getLookAngle().normalize();
        for (int i = 0; i < WeaponLevels.pick(SKULL_COUNT, weaponLevel); i++) {
            WitherRainSkull skull = new WitherRainSkull(level, owner, weaponLevel);
            skull.setPos(start.x, start.y, start.z);
            Vec3 spread = new Vec3(level.getRandom().nextDouble() * 0.5 - 0.25, 0, level.getRandom().nextDouble() * 0.5 - 0.25);
            skull.setDeltaMovement(forward.add(spread).normalize().scale(SPEED));
            level.addFreshEntity(skull);
        }
        level.playSound(null, owner.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.6F, 1.2F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level) || !this.isAlive()) return;

        // Keeps the speed, and turns towards the closest monster (level III and up)
        Vec3 direction = this.getDeltaMovement().normalize();
        if (WeaponLevels.pick(HOMING, weaponLevel)) {
            LivingEntity target = closestMonster(level);
            if (target != null) {
                Vec3 toTarget = target.getBoundingBox().getCenter().subtract(this.position()).normalize();
                direction = direction.lerp(toTarget, TURN_STRENGTH).normalize();
            }
        }
        this.setDeltaMovement(direction.scale(SPEED));
    }

    private LivingEntity closestMonster(ServerLevel level) {
        LivingEntity closest = null;
        double best = HOMING_RANGE * HOMING_RANGE;
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(HOMING_RANGE),
                mob -> mob instanceof Enemy && mob.isAlive() && mob != this.getOwner())) {
            double distance = mob.distanceToSqr(this);
            if (distance < best) {
                best = distance;
                closest = mob;
            }
        }
        return closest;
    }

    // The skulls never hit the player who launched them
    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity != this.getOwner() && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!(this.level() instanceof ServerLevel level)) return;
        if (hitResult.getEntity().hurtServer(level, damageSource(), WeaponLevels.pick(DAMAGE, weaponLevel))
                && hitResult.getEntity() instanceof LivingEntity target) {
            wither(target);
            reward(target);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) hitResult);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) hitResult);
        }
        if (this.level() instanceof ServerLevel level) {
            explode(level, hitResult instanceof EntityHitResult entityHit ? entityHit.getEntity() : null);
            this.discard();
        }
    }

    /// Damage-only explosion: hurts the mobs around, nothing else.
    private void explode(ServerLevel level, Entity hitEntity) {
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.4F, 1.4F);
        float radius = WeaponLevels.pick(EXPLOSION_RADIUS, weaponLevel);
        float damage = WeaponLevels.pick(EXPLOSION_DAMAGE, weaponLevel);
        if (radius <= 0 || damage <= 0) return;

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
            // The mob hit directly already took the skull's damage
            if (target == this.getOwner() || target == hitEntity || isHoldingWitherSword(target)) continue;
            double distance = target.position().distanceTo(this.position());
            if (distance > radius) continue;
            if (target.hurtServer(level, damageSource(), (float) (damage * (1.0 - distance / radius)))) reward(target);
        }
    }

    /// Wither of this level (Wither III for the blue skulls).
    private void wither(LivingEntity target) {
        int seconds = WeaponLevels.pick(WITHER_SECONDS, weaponLevel);
        if (seconds > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, seconds * 20, WeaponLevels.pick(WITHER_AMPLIFIER, weaponLevel)), this.getEffectSource());
        }
    }

    private void reward(LivingEntity target) {
        if (this.getOwner() instanceof LivingEntity owner) WeaponLevels.reward(owner, ModItems.WITHER_SWORD.get(), target);
    }

    private DamageSource damageSource() {
        return this.getOwner() instanceof LivingEntity owner ? this.damageSources().witherSkull(this, owner) : this.damageSources().magic();
    }

    private static boolean isHoldingWitherSword(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof WitherSwordItem
                || entity.getOffhandItem().getItem() instanceof WitherSwordItem;
    }
}
