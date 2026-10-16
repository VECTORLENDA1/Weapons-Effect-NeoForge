package com.exoticmoss.weaponseffect.entity.custom;

import java.util.UUID;
import org.jspecify.annotations.Nullable;
import com.exoticmoss.weaponseffect.util.EntityLimit;
import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModItems;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/// The Gimli's Axe ability ("Boomerang Axe"): a spinning axe flies to where the player is aiming and comes
/// back to the player, like a boomerang. It hurts and slows every mob it touches. It turns back at walls.
///  - Level III and up: the axe also hits the mobs again on the way back.
///  - Level V: three axes at once.
/// Drawn by ThrownAxeRenderer (the axe itself, spinning).
///
/// TABLES: one number for each level { I, II, III, IV, V }.
public class ThrownAxeEntity extends Entity implements EntityLimit.Mergeable {
    /// Most of these a player can have at once (more would make the game lag, see EntityLimit).
    /// Over this, new ones give their power to the closest one instead of being created.
    public static final int MAX_ALIVE = 12;

    /// How far the axe flies before coming back (blocks).
    public static final float[] RANGE = {10, 12, 14, 16, 18};
    /// Damage to each mob it touches, and Slowness given to it (seconds).
    public static final float[] DAMAGE = {10, 12, 14, 16, 20};
    public static final int[] SLOWNESS_SECONDS = {2, 2, 3, 3, 4};
    /// Levels where the axe hits the mobs again on the way back.
    public static final boolean[] HIT_ON_RETURN = {false, false, true, true, true};
    /// How many axes are thrown each time.
    public static final int[] AXE_COUNT = {1, 1, 1, 1, 3};

    /// Speed of the axe going and coming back (blocks per tick).
    public static final double SPEED = 1.2;
    public static final double RETURN_SPEED = 1.4;
    /// Angle between the axes when there are more than one (degrees).
    public static final float SPREAD_DEGREES = 15;
    /// Safety: the axe disappears after this many ticks even if it didn't come back.
    public static final int MAX_TICKS = 200;

    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.ITEM_STACK);

    private LivingEntity owner;
    private int weaponLevel = 1;
    /// How many projectiles this one counts as (it does their damage too, see EntityLimit).
    private int power = 1;
    private Vec3 start = Vec3.ZERO;
    private boolean returning;
    private final Set<LivingEntity> hitGoing = new HashSet<>();
    private final Set<LivingEntity> hitReturning = new HashSet<>();

    public ThrownAxeEntity(EntityType<? extends ThrownAxeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /// The ability: throws the axe(s) to where the player is aiming.
    public static void launch(ServerLevel level, Player owner, ItemStack weapon, int weaponLevel) {
        int count = WeaponLevels.pick(AXE_COUNT, weaponLevel);
        for (int i = 0; i < count; i++) {
            float yaw = owner.getYRot() + (i - (count - 1) / 2F) * SPREAD_DEGREES;
            ThrownAxeEntity axe = new ThrownAxeEntity(ModEntities.THROWN_AXE.get(), level);
            axe.owner = owner;
            axe.weaponLevel = weaponLevel;
            axe.entityData.set(DATA_ITEM, weapon.copyWithCount(1));
            axe.start = owner.getEyePosition().add(0, -0.3, 0);
            axe.setPos(axe.start.x, axe.start.y, axe.start.z);
            axe.setYRot(yaw);
            axe.setDeltaMovement(Vec3.directionFromRotation(owner.getXRot(), yaw).scale(SPEED));
            EntityLimit.spawnOrMerge(level, axe, MAX_ALIVE);
        }
        level.playSound(null, owner.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 0.7F);
    }

    public ItemStack getItem() {
        return this.entityData.get(DATA_ITEM);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (owner == null || !owner.isAlive() || owner.level() != level || this.tickCount > MAX_TICKS) {
            this.discard();
            return;
        }

        Vec3 motion;
        if (!returning) {
            motion = this.getDeltaMovement();
            // Turns back at the end of its range, or when it is about to hit a wall
            HitResult wall = level.clip(new ClipContext(this.position(), this.position().add(motion), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (wall.getType() != HitResult.Type.MISS || this.position().distanceTo(start) >= WeaponLevels.pick(RANGE, weaponLevel)) {
                returning = true;
                motion = Vec3.ZERO;
            }
        } else {
            Vec3 toOwner = owner.getEyePosition().add(0, -0.3, 0).subtract(this.position());
            if (toOwner.length() < 1.5) {
                level.playSound(null, owner.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
                this.discard();
                return;
            }
            motion = toOwner.normalize().scale(Math.min(RETURN_SPEED, toOwner.length()));
        }
        this.setDeltaMovement(motion);
        this.setPos(this.position().add(motion));
        if (this.tickCount % 4 == 0) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.4F, 1.5F);
        }
        hurtMobs(level);
    }

    /// Damage: every mob the axe touches (once going and, from level III, once more coming back).
    private void hurtMobs(ServerLevel level) {
        if (returning && !HIT_ON_RETURN[weaponLevel - 1]) return;
        Set<LivingEntity> hit = returning ? hitReturning : hitGoing;
        DamageSource source = owner instanceof Player player ? player.damageSources().playerAttack(player) : owner.damageSources().mobAttack(owner);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.4),
                target -> target != owner && !hit.contains(target))) {
            hit.add(target);
            if (target.hurtServer(level, source, WeaponLevels.pick(DAMAGE, weaponLevel) * power)) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, WeaponLevels.pick(SLOWNESS_SECONDS, weaponLevel) * 20, 2), owner);
                level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
                WeaponLevels.reward(owner, ModItems.GIMLIS_AXE.get(), target);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    // The axe only lives for a moment: it is not saved with the world
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

    @Override
    public @Nullable UUID abilityOwnerId() {
        return owner == null ? null : owner.getUUID();
    }

    @Override
    public void absorb(int count) {
        power += count;
    }
}
