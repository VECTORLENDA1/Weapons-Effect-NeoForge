package com.exoticmoss.weaponseffect.entity.custom;

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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashSet;
import java.util.Set;

/// The Blindness Dagger ability ("Smoke Bomb"): a cloud of black smoke around the player.
/// The mobs inside are blinded and lose the player; the player becomes invisible.
///  - Level III and up: the mobs inside keep losing the player while the smoke lasts (not only at the start).
///  - Level V: the mobs inside also get Weakness, and the player gets Speed.
/// Drawn in 3D by SmokeBombRenderer (no particles).
///
/// TABLES: one number for each level { I, II, III, IV, V }.
public class SmokeBombEntity extends Entity {
    /// Size of the smoke (blocks) and how long it stays (seconds).
    public static final float[] RADIUS = {4, 4.5F, 5, 5.5F, 6};
    public static final int[] SMOKE_SECONDS = {5, 6, 7, 8, 10};
    /// Blindness given to the mobs inside (seconds).
    public static final int[] BLINDNESS_SECONDS = {4, 5, 6, 7, 8};
    /// Invisibility given to the player (seconds).
    public static final int[] INVISIBILITY_SECONDS = {6, 7, 8, 10, 12};
    /// Levels where the mobs inside keep losing the player while the smoke lasts.
    public static final boolean[] KEEP_LOST = {false, false, true, true, true};
    /// Weakness II given to the mobs inside and Speed II given to the player (seconds, 0 = none).
    public static final int[] WEAKNESS_SECONDS = {0, 0, 0, 0, 6};
    public static final int[] SPEED_SECONDS = {0, 0, 0, 0, 8};

    /// Ticks the smoke takes to grow and to fade away (20 ticks = 1 second).
    public static final int GROW_TICKS = 10;
    public static final int FADE_TICKS = 20;
    /// Every how many ticks the smoke works on the mobs inside.
    public static final int EFFECT_INTERVAL = 10;

    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(SmokeBombEntity.class, EntityDataSerializers.INT);

    private LivingEntity owner;
    private final Set<LivingEntity> rewarded = new HashSet<>();

    public SmokeBombEntity(EntityType<? extends SmokeBombEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /// The ability: the smoke appears around the player.
    public static void launch(ServerLevel level, Player owner, int weaponLevel) {
        SmokeBombEntity smoke = new SmokeBombEntity(ModEntities.SMOKE_BOMB.get(), level);
        smoke.owner = owner;
        smoke.entityData.set(DATA_LEVEL, weaponLevel);
        smoke.setPos(owner.getX(), owner.getY(), owner.getZ());
        level.addFreshEntity(smoke);

        owner.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, WeaponLevels.pick(INVISIBILITY_SECONDS, weaponLevel) * 20, 0, false, false, true));
        int speed = WeaponLevels.pick(SPEED_SECONDS, weaponLevel);
        if (speed > 0) owner.addEffect(new MobEffectInstance(MobEffects.SPEED, speed * 20, 1));
        level.playSound(null, owner.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5F, 1.8F);
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.2F, 0.6F);
        smoke.affectMobs(level, true);
    }

    public int getWeaponLevel() {
        return this.entityData.get(DATA_LEVEL);
    }

    public float getRadius() {
        return WeaponLevels.pick(RADIUS, getWeaponLevel());
    }

    public int getLifetime() {
        return WeaponLevels.pick(SMOKE_SECONDS, getWeaponLevel()) * 20;
    }

    /// From 0 to 1: how big the smoke is (it grows at the start).
    public float getSize(float partialTicks) {
        return Mth.clamp((this.tickCount + partialTicks) / GROW_TICKS, 0, 1);
    }

    /// From 1 to 0: how dark the smoke is (it fades at the end).
    public float getOpacity(float partialTicks) {
        return Mth.clamp((getLifetime() - this.tickCount - partialTicks) / FADE_TICKS, 0, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (owner == null || this.tickCount > getLifetime()) {
            this.discard();
            return;
        }
        if (this.tickCount % EFFECT_INTERVAL == 0) affectMobs(level, KEEP_LOST[getWeaponLevel() - 1]);
    }

    /// Blinds the mobs inside; if "lose" is true they also stop chasing the player.
    private void affectMobs(ServerLevel level, boolean lose) {
        int weaponLevel = getWeaponLevel();
        float radius = getRadius();
        int weakness = WeaponLevels.pick(WEAKNESS_SECONDS, weaponLevel);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius),
                mob -> mob != owner && !(mob instanceof Player) && mob.distanceTo(this) <= radius)) {
            mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, WeaponLevels.pick(BLINDNESS_SECONDS, weaponLevel) * 20, 0), owner);
            if (weakness > 0) mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, weakness * 20, 1), owner);
            if (lose && mob instanceof Mob monster && monster.getTarget() == owner) monster.setTarget(null);
            if (rewarded.add(mob)) WeaponLevels.reward(owner, ModItems.BLINDNESS_DAGGER.get(), mob);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LEVEL, 1);
    }

    // The smoke only lives for a moment: it is not saved with the world
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
