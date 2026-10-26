package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModItems;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import com.exoticmoss.weaponseffect.util.Aim;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/// The Poison Sword ability ("Poison Cloud"): a cloud of poison where the player is aiming.
/// It grows, stays for a while and fades away (keeping its size), poisoning every mob inside it.
///  - Level III and up: the cloud slowly follows the closest monster.
///  - Level V: it also gives Wither and Nausea.
/// Drawn in 3D by PoisonCloudRenderer (the same size where mobs get poisoned).
///
/// TABLES: one number for each level { I, II, III, IV, V }.
public class PoisonCloudEntity extends Entity {
    /// Size of the cloud (blocks): radius and height.
    public static final float[] RADIUS = {4, 4.5F, 5, 5.5F, 6};
    public static final float[] HEIGHT = {2.5F, 2.75F, 3, 3.25F, 3.5F};
    /// How long the cloud lasts (ticks, 20 = 1 second).
    public static final int[] DURATION = {200, 240, 280, 320, 400};
    /// The poison given to the mobs inside: seconds and level (0 = Poison I, 1 = Poison II...).
    public static final int[] POISON_SECONDS = {4, 4, 5, 5, 6};
    public static final int[] POISON_AMPLIFIER = {1, 1, 2, 2, 3};
    /// Levels where the cloud follows the closest monster.
    public static final boolean[] FOLLOW = {false, false, true, true, true};
    /// Wither and Nausea given to the mobs inside (seconds, 0 = none).
    public static final int[] WITHER_SECONDS = {0, 0, 0, 0, 4};
    public static final int[] NAUSEA_SECONDS = {0, 0, 0, 0, 6};

    /// How long the cloud takes to grow at the start and to fade away at the end (ticks).
    public static final int GROW_TICKS = 20;
    public static final int FADE_TICKS = 30;
    /// How far the player can aim the cloud (blocks).
    public static final float RANGE = 20.0F;
    /// Following: how far it looks for a monster (blocks) and how fast it moves (blocks per tick).
    public static final double FOLLOW_RANGE = 16;
    public static final double FOLLOW_SPEED = 0.12;

    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(PoisonCloudEntity.class, EntityDataSerializers.INT);

    private LivingEntity owner;

    public PoisonCloudEntity(EntityType<? extends PoisonCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /// The ability: a poison cloud where the player is aiming.
    public static void launch(ServerLevel level, LivingEntity owner, int weaponLevel) {
        Vec3 target = Aim.target(owner, RANGE);
        PoisonCloudEntity cloud = new PoisonCloudEntity(ModEntities.POISON_CLOUD.get(), level);
        cloud.owner = owner;
        cloud.entityData.set(DATA_LEVEL, weaponLevel);
        cloud.setPos(target.x, target.y, target.z);
        level.addFreshEntity(cloud);
        level.playSound(null, target.x, target.y, target.z, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.5F, 0.6F);
    }

    public int getWeaponLevel() {
        return this.entityData.get(DATA_LEVEL);
    }

    public float getRadius() {
        return WeaponLevels.pick(RADIUS, getWeaponLevel());
    }

    public float getHeight() {
        return WeaponLevels.pick(HEIGHT, getWeaponLevel());
    }

    /// Size of the cloud right now, from 0 to 1 (grows at the start).
    public float getSize(float partialTicks) {
        return Mth.clamp((this.tickCount + partialTicks) / GROW_TICKS, 0, 1);
    }

    /// How visible the cloud is, from 1 to 0 (fades away at the end).
    public float getOpacity(float partialTicks) {
        return Mth.clamp((WeaponLevels.pick(DURATION, getWeaponLevel()) - this.tickCount - partialTicks) / FADE_TICKS, 0, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.tickCount >= WeaponLevels.pick(DURATION, getWeaponLevel())) {
            this.discard();
            return;
        }
        if (WeaponLevels.pick(FOLLOW, getWeaponLevel())) followClosestMonster(level);
        // Poisons the mobs inside every half second
        if (this.tickCount % 10 == 0) poisonMobsInside(level);
    }

    /// Level III and up: the cloud slowly moves to the closest monster.
    private void followClosestMonster(ServerLevel level) {
        LivingEntity closest = null;
        double best = FOLLOW_RANGE * FOLLOW_RANGE;
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(FOLLOW_RANGE),
                mob -> mob instanceof Enemy && mob.isAlive())) {
            double distance = mob.distanceToSqr(this);
            if (distance < best) {
                best = distance;
                closest = mob;
            }
        }
        if (closest == null) return;
        Vec3 toMob = closest.position().subtract(this.position());
        if (toMob.length() < 0.5) return;
        Vec3 step = toMob.normalize().scale(Math.min(FOLLOW_SPEED, toMob.length()));
        this.setPos(this.position().add(step));
    }

    private void poisonMobsInside(ServerLevel level) {
        int weaponLevel = getWeaponLevel();
        float radius = getRadius() * getSize(0);
        AABB area = new AABB(this.getX() - radius, this.getY(), this.getZ() - radius,
                this.getX() + radius, this.getY() + getHeight(), this.getZ() + radius);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (mob == owner || mob.position().subtract(this.position()).horizontalDistance() > radius) continue;
            mob.addEffect(new MobEffectInstance(MobEffects.POISON, WeaponLevels.pick(POISON_SECONDS, weaponLevel) * 20,
                    WeaponLevels.pick(POISON_AMPLIFIER, weaponLevel)), owner);
            int wither = WeaponLevels.pick(WITHER_SECONDS, weaponLevel);
            if (wither > 0) mob.addEffect(new MobEffectInstance(MobEffects.WITHER, wither * 20, 1), owner);
            int nausea = WeaponLevels.pick(NAUSEA_SECONDS, weaponLevel);
            if (nausea > 0) mob.addEffect(new MobEffectInstance(MobEffects.NAUSEA, nausea * 20, 0), owner);
            if (owner != null) WeaponLevels.reward(owner, ModItems.POISON_SWORD.get(), mob);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LEVEL, 1);
    }

    // The cloud only lives for a moment: it is not saved with the world
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
