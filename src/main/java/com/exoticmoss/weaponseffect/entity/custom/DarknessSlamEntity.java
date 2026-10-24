package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.effect.ModEffect;
import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModItems;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/// The Darkness Mace ability ("Dark Slam"): the player leaps forward and, when they land, the mace slams the
/// ground. The slam hurts the mobs around and leaves a zone of darkness on the ground. Mobs touching the
/// zone become Disoriented: they forget their target, walk in zigzags and can't hurt anyone (see DisorientedEffect).
/// No fall damage from the leap.
///  - Level III and up: the slam throws the mobs up in the air, and the zone also slows the mobs.
/// While the player is in the air this entity follows them (invisible); then it stays as the zone
/// (drawn by DarknessSlamRenderer).
///
/// TABLES: one number for each level { I, II, III, IV, V }.
public class DarknessSlamEntity extends Entity {
    /// The leap: speed up and forward (bigger = higher and longer).
    public static final double[] LEAP_UP = {0.9, 0.95, 1.0, 1.05, 1.1};
    public static final double[] LEAP_FORWARD = {0.9, 1.0, 1.1, 1.2, 1.3};
    /// The slam: radius (blocks), damage, and how high the mobs are thrown (0 = not thrown).
    public static final float[] SLAM_RADIUS = {4, 4.5F, 5, 5.5F, 6};
    public static final float[] SLAM_DAMAGE = {12, 14, 16, 18, 22};
    public static final double[] KNOCK_UP = {0, 0, 0.6, 0.7, 0.8};
    /// The darkness zone: radius (blocks) and how long it stays (seconds).
    public static final float[] ZONE_RADIUS = {3, 3.5F, 4, 4.5F, 5};
    public static final int[] ZONE_SECONDS = {4, 5, 6, 7, 8};
    /// Disoriented given to the mobs touching the zone (seconds).
    public static final int[] DISORIENTED_SECONDS = {3, 3, 4, 5, 6};
    /// Slowness III given by the zone (seconds, 0 = none).
    public static final int[] ZONE_SLOWNESS_SECONDS = {0, 0, 2, 2, 3};

    /// Color of the slam's shockwave (0xAARRGGBB).
    public static final int SLAM_COLOR = 0x80301050;
    /// Ticks in the air before the slam can happen, and the most ticks it waits for the player to land.
    public static final int MIN_AIR_TICKS = 4;
    public static final int MAX_AIR_TICKS = 80;
    /// Ticks the zone takes to grow and to fade away; every how many ticks it works on the mobs.
    public static final int GROW_TICKS = 8;
    public static final int FADE_TICKS = 20;
    public static final int EFFECT_INTERVAL = 5;

    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(DarknessSlamEntity.class, EntityDataSerializers.INT);
    /// The tick when the player landed (-1 = still in the air).
    private static final EntityDataAccessor<Integer> DATA_LANDED_AT = SynchedEntityData.defineId(DarknessSlamEntity.class, EntityDataSerializers.INT);

    private LivingEntity owner;
    private final Set<LivingEntity> rewarded = new HashSet<>();

    public DarknessSlamEntity(EntityType<? extends DarknessSlamEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /// The ability: the player leaps forward (the slam happens when they land).
    public static void leap(ServerLevel level, Player owner, int weaponLevel) {
        DarknessSlamEntity slam = new DarknessSlamEntity(ModEntities.DARKNESS_SLAM, level);
        slam.owner = owner;
        slam.entityData.set(DATA_LEVEL, weaponLevel);
        slam.setPos(owner.getX(), owner.getY(), owner.getZ());
        level.addFreshEntity(slam);

        Vec3 forward = owner.getLookAngle().multiply(1, 0, 1).normalize().scale(WeaponLevels.pick(LEAP_FORWARD, weaponLevel));
        owner.setDeltaMovement(forward.x, WeaponLevels.pick(LEAP_UP, weaponLevel), forward.z);
        owner.hurtMarked = true;
        level.playSound(null, owner.blockPosition(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    public int getWeaponLevel() {
        return this.entityData.get(DATA_LEVEL);
    }

    public boolean hasLanded() {
        return this.entityData.get(DATA_LANDED_AT) >= 0;
    }

    public float getZoneRadius() {
        return WeaponLevels.pick(ZONE_RADIUS, getWeaponLevel());
    }

    /// Ticks since the slam (the life of the zone).
    private float zoneAge(float partialTicks) {
        return this.tickCount + partialTicks - this.entityData.get(DATA_LANDED_AT);
    }

    /// From 0 to 1: how big the zone is (it grows right after the slam).
    public float getZoneSize(float partialTicks) {
        return Mth.clamp(zoneAge(partialTicks) / GROW_TICKS, 0, 1);
    }

    /// From 1 to 0: how dark the zone is (it fades at the end).
    public float getZoneOpacity(float partialTicks) {
        return Mth.clamp((WeaponLevels.pick(ZONE_SECONDS, getWeaponLevel()) * 20 - zoneAge(partialTicks)) / FADE_TICKS, 0, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (owner == null || owner.level() != level) {
            this.discard();
            return;
        }

        if (!hasLanded()) {
            // In the air: follows the player until they land
            if (!owner.isAlive()) {
                this.discard();
                return;
            }
            owner.resetFallDistance();
            this.setPos(owner.getX(), owner.getY(), owner.getZ());
            if ((this.tickCount > MIN_AIR_TICKS && owner.onGround()) || this.tickCount > MAX_AIR_TICKS) slam(level);
            return;
        }

        // The zone
        if (zoneAge(0) > WeaponLevels.pick(ZONE_SECONDS, getWeaponLevel()) * 20) {
            this.discard();
            return;
        }
        if (this.tickCount % EFFECT_INTERVAL == 0) disorientMobs(level);
    }

    /// The slam: damage around the player and the start of the zone.
    private void slam(ServerLevel level) {
        int weaponLevel = getWeaponLevel();
        this.entityData.set(DATA_LANDED_AT, this.tickCount);
        ShockwaveEntity.blast(level, this.position(), WeaponLevels.pick(SLAM_RADIUS, weaponLevel), SLAM_COLOR, 0,
                new ShockwaveEntity.Blast(owner, ModItems.DARKNESS_MACE, WeaponLevels.pick(SLAM_DAMAGE, weaponLevel),
                        0, WeaponLevels.pick(KNOCK_UP, weaponLevel), 0));
        level.playSound(null, this.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    /// The mobs touching the zone become Disoriented (and slow from level III).
    private void disorientMobs(ServerLevel level) {
        int weaponLevel = getWeaponLevel();
        float radius = getZoneRadius();
        int slowness = WeaponLevels.pick(ZONE_SLOWNESS_SECONDS, weaponLevel);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius, 2, radius),
                mob -> mob != owner && !(mob instanceof Player)
                        && mob.position().subtract(this.position()).horizontalDistance() <= radius + mob.getBbWidth() / 2)) {
            mob.addEffect(new MobEffectInstance(ModEffect.DISORIENTED, WeaponLevels.pick(DISORIENTED_SECONDS, weaponLevel) * 20, 0), owner);
            if (slowness > 0) mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, slowness * 20, 2), owner);
            if (rewarded.add(mob)) WeaponLevels.reward(owner, ModItems.DARKNESS_MACE, mob);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LEVEL, 1);
        builder.define(DATA_LANDED_AT, -1);
    }

    // The slam only lives for a moment: it is not saved with the world
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
