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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// The Fire Sword ability ("Blazing Gaze"): ribbons of fire and lava twisting around each other, shot from
/// the sword to where the player is aiming. They spread out a bit, stop at walls, burn and hurt every mob they touch.
///  - Level III and up: the flames leave fire on the ground under their path for a few seconds.
///  - Level V: blue soul fire.
/// The entity stays at the sword; the flames are drawn by FlameWaveRenderer (no particles).
///
/// TABLES: one number for each level { I, II, III, IV, V }.
public class FlameWaveEntity extends Entity {
    /// How far the flames reach, measured from the player (blocks). They stop earlier at walls.
    public static final float[] LENGTH = {8, 9, 10, 11, 12};
    /// Total width of the flames at the end (blocks). Mobs touching this width are hit.
    public static final float[] WIDTH = {4, 4.5F, 5, 5.5F, 6};
    /// Damage to each mob the flames hit (only once) and how long it burns (seconds).
    public static final float[] DAMAGE = {8, 10, 12, 14, 18};
    public static final int[] FIRE_SECONDS = {5, 6, 7, 8, 10};
    /// Fire on the ground under the path: how long it stays (seconds, 0 = none) and its damage every half second.
    public static final int[] GROUND_FIRE_SECONDS = {0, 0, 4, 5, 6};
    public static final float[] GROUND_FIRE_DAMAGE = {0, 0, 2, 3, 4};
    /// Levels with blue soul fire.
    public static final boolean[] SOUL_FIRE = {false, false, false, false, true};

    /// How many blocks the front of the flames moves each tick (20 ticks = 1 second).
    public static final float SPEED = 0.6F;
    /// Ticks the flames stay at full length (connected to the sword) before their end leaves the sword.
    public static final int HOLD = 4;
    /// How many ribbons of fire twist around each other.
    public static final int RIBBONS = 10;
    /// Width of the flames at the sword (blocks).
    public static final float START_WIDTH = 0.3F;
    /// How far in front of the player's eyes the flames start (the sword), in blocks.
    public static final float START_DISTANCE = 0.5F;

    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(FlameWaveEntity.class, EntityDataSerializers.INT);

    private LivingEntity owner;
    private final Set<LivingEntity> hit = new HashSet<>();
    /// Where the flames stop (a wall or LENGTH). Calculated once, on the server and on the client.
    private float maxLength = -1;
    /// The places on the ground under the path (for the ground fire). Calculated once.
    private List<Vec3> groundFire;

    public FlameWaveEntity(EntityType<? extends FlameWaveEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    private FlameWaveEntity(Level level, LivingEntity owner, int weaponLevel) {
        this(ModEntities.FLAME_WAVE.get(), level);
        this.owner = owner;
        this.entityData.set(DATA_LEVEL, weaponLevel);
        // Comes out of the sword, a bit in front of the player, in the direction the player is aiming
        Vec3 start = owner.getEyePosition().add(0, -0.35, 0).add(owner.getLookAngle().scale(START_DISTANCE));
        this.setPos(start.x, start.y, start.z);
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
    }

    /// The ability: shoots the flames to where the player is aiming.
    public static void launch(ServerLevel level, LivingEntity owner, int weaponLevel) {
        level.addFreshEntity(new FlameWaveEntity(level, owner, weaponLevel));
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.6F);
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 4.0F, 1.0F);
    }

    // ---------------------------------------------------------------- shape (same on server and client)

    public int getWeaponLevel() {
        return this.entityData.get(DATA_LEVEL);
    }

    public boolean isSoulFire() {
        return SOUL_FIRE[getWeaponLevel() - 1];
    }

    /// The direction of the flames (where the player was aiming).
    public Vec3 getFlameDirection() {
        return Vec3.directionFromRotation(this.getXRot(), this.getYRot());
    }

    /// The length of the flames from the sword (LENGTH is counted from the player).
    public float reach() {
        return Math.max(0.5F, WeaponLevels.pick(LENGTH, getWeaponLevel()) - START_DISTANCE);
    }

    /// Half of the width of the flames at this distance from the sword (middle line to outer edge).
    public float halfWidthAt(float distance) {
        return Mth.lerp(Mth.clamp(distance / reach(), 0, 1), START_WIDTH, WeaponLevels.pick(WIDTH, getWeaponLevel())) / 2;
    }

    /// Where the flames stop (from the sword): at the first wall, or at LENGTH from the player.
    public float getMaxLength() {
        if (maxLength < 0) {
            Vec3 start = this.position();
            Vec3 end = start.add(getFlameDirection().scale(reach()));
            HitResult result = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            maxLength = result.getType() == HitResult.Type.MISS ? reach() : (float) result.getLocation().distanceTo(start);
        }
        return maxLength;
    }

    /// How far the front of the flames is from the sword.
    public float getHead(float partialTicks) {
        return Math.min(getMaxLength(), SPEED * (this.tickCount + partialTicks));
    }

    /// How far the end of the flames is from the sword (0 while they are still coming out).
    public float getTail(float partialTicks) {
        float max = getMaxLength();
        return Mth.clamp(SPEED * (this.tickCount + partialTicks - HOLD) - max, 0, max);
    }

    /// Ticks the whole ability lasts: the flames, and then the fire on the ground.
    public int getLifetime() {
        return Mth.ceil(2 * getMaxLength() / SPEED) + HOLD + WeaponLevels.pick(GROUND_FIRE_SECONDS, getWeaponLevel()) * 20;
    }

    /// The fire on the ground: one place every block under the path of the flames (empty if this level has none).
    public List<Vec3> getGroundFire() {
        if (groundFire == null) {
            groundFire = new ArrayList<>();
            if (WeaponLevels.pick(GROUND_FIRE_SECONDS, getWeaponLevel()) > 0) {
                for (float along = 1; along <= getMaxLength(); along += 1) {
                    Vec3 point = this.position().add(getFlameDirection().scale(along));
                    HitResult ground = this.level().clip(new ClipContext(point, point.add(0, -6, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    if (ground.getType() != HitResult.Type.MISS) groundFire.add(ground.getLocation());
                }
            }
        }
        return groundFire;
    }

    /// From 1 to 0: how strong the ground fire is (it fades in the last second).
    public float getGroundFireStrength(float partialTicks) {
        return Mth.clamp((getLifetime() - this.tickCount - partialTicks) / 20F, 0, 1);
    }

    // ---------------------------------------------------------------- server

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        if (owner == null || !owner.isAlive() || this.tickCount > getLifetime()) {
            this.discard();
            return;
        }
        if (getTail(0) < getMaxLength()) hurtMobs(level);
        if (this.tickCount % 10 == 0) burnGround(level);
    }

    /// Damage: every mob touching the flames (each one only once).
    private void hurtMobs(ServerLevel level) {
        int weaponLevel = getWeaponLevel();
        float head = getHead(0);
        float tail = getTail(0);
        Vec3 start = this.position();
        Vec3 direction = getFlameDirection();
        AABB area = new AABB(start.add(direction.scale(tail)), start.add(direction.scale(head))).inflate(WeaponLevels.pick(WIDTH, weaponLevel) / 2 + 1);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == owner || target.fireImmune() || hit.contains(target)) continue;

            // Distance along the flames and from the middle line; the mob is hit when its body touches them
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(start);
            double along = toTarget.dot(direction);
            double body = target.getBbWidth() / 2;
            if (along < tail - body || along > head + body) continue;
            if (toTarget.subtract(direction.scale(along)).length() > halfWidthAt((float) along) + body) continue;

            hit.add(target);
            target.setRemainingFireTicks(WeaponLevels.pick(FIRE_SECONDS, weaponLevel) * 20);
            target.hurtServer(level, damageSource(owner), WeaponLevels.pick(DAMAGE, weaponLevel));
            WeaponLevels.reward(owner, ModItems.FIRE_SWORD.get(), target);
        }
    }

    /// The fire on the ground burns the mobs standing on it (only where the flames already passed).
    private void burnGround(ServerLevel level) {
        int weaponLevel = getWeaponLevel();
        float damage = WeaponLevels.pick(GROUND_FIRE_DAMAGE, weaponLevel);
        if (damage <= 0) return;
        float head = getHead(0);
        for (Vec3 fire : getGroundFire()) {
            if (fire.distanceTo(this.position()) > head + 1) continue;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(fire, fire).inflate(0.8, 1.5, 0.8))) {
                if (target == owner || target.fireImmune()) continue;
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 60));
                target.hurtServer(level, damageSource(owner), damage);
                WeaponLevels.reward(owner, ModItems.FIRE_SWORD.get(), target);
            }
        }
    }

    private static DamageSource damageSource(LivingEntity attacker) {
        return attacker instanceof Player player
                ? player.damageSources().playerAttack(player)
                : attacker.damageSources().mobAttack(attacker);
    }

    // ---------------------------------------------------------------- data

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LEVEL, 1);
    }

    // When the level arrives on the client, the shape is calculated again
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (DATA_LEVEL.equals(data)) {
            maxLength = -1;
            groundFire = null;
        }
    }

    // The flames only live for a moment: they are not saved with the world
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
