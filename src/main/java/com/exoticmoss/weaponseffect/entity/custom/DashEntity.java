package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModItems;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.syncher.SynchedEntityData;
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

/// The Swiftness Dagger ability ("Dash"): the player shoots forward to where they are aiming and hurts
/// every mob they pass through. No fall damage during the dash.
///  - Level III and up: the mobs passed through are also poisoned.
/// This entity follows the player during the dash (DashRenderer draws the green trail).
///
/// TABLES: one number for each level { I, II, III, IV, V }.
public class DashEntity extends Entity {
    /// How far the dash goes (blocks). It stops earlier at walls.
    public static final float[] DISTANCE = {6, 7, 8, 9, 10};
    /// Damage to each mob passed through (only once).
    public static final float[] DAMAGE = {6, 8, 10, 12, 15};
    /// Poison given to the mobs passed through: seconds (0 = none) and level (0 = Poison I).
    public static final int[] POISON_SECONDS = {0, 0, 4, 5, 6};
    public static final int[] POISON_AMPLIFIER = {0, 0, 1, 1, 2};

    /// How many ticks the dash lasts (20 ticks = 1 second).
    public static final int DASH_TICKS = 5;
    /// How far from the player's body a mob is still hit (blocks).
    public static final double HIT_WIDTH = 0.8;
    /// How much the dash can go up or down (1 = all the way where the player is aiming, 0 = only flat).
    public static final double VERTICAL = 0.5;
    /// Ticks after the dash without fall damage.
    public static final int SAFE_LANDING_TICKS = 30;

    private LivingEntity owner;
    private Vec3 direction = Vec3.ZERO;
    private int weaponLevel = 1;
    private final Set<LivingEntity> hit = new HashSet<>();

    public DashEntity(EntityType<? extends DashEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /// The ability: the player dashes to where they are aiming.
    public static void launch(ServerLevel level, Player owner, int weaponLevel) {
        DashEntity dash = new DashEntity(ModEntities.DASH.get(), level);
        dash.owner = owner;
        dash.weaponLevel = weaponLevel;
        Vec3 look = owner.getLookAngle();
        dash.direction = new Vec3(look.x, look.y * VERTICAL, look.z).normalize();
        dash.setPos(owner.getX(), owner.getY(), owner.getZ());
        dash.setYRot(owner.getYRot());
        dash.setXRot((float) -Math.toDegrees(Math.asin(dash.direction.y)));
        level.addFreshEntity(dash);
        level.playSound(null, owner.blockPosition(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    /// From 0 (start) to 1 (end of the dash), for the trail.
    public float getProgress(float partialTicks) {
        return Mth.clamp((this.tickCount + partialTicks) / DASH_TICKS, 0, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (owner == null || !owner.isAlive() || this.tickCount > DASH_TICKS + SAFE_LANDING_TICKS) {
            this.discard();
            return;
        }
        owner.resetFallDistance();
        if (this.tickCount > DASH_TICKS) return;

        // Pushes the player (every tick, so the dash keeps its speed), then slows them down at the end
        double speed = WeaponLevels.pick(DISTANCE, weaponLevel) / DASH_TICKS;
        owner.setDeltaMovement(direction.scale(this.tickCount == DASH_TICKS ? 0.3 : speed));
        owner.hurtMarked = true;
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        hurtMobs(level);
    }

    /// Damage: every mob the player passes through (each one only once).
    private void hurtMobs(ServerLevel level) {
        DamageSource source = owner instanceof Player player ? player.damageSources().playerAttack(player) : owner.damageSources().mobAttack(owner);
        int poison = WeaponLevels.pick(POISON_SECONDS, weaponLevel);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(HIT_WIDTH),
                target -> target != owner && !hit.contains(target))) {
            hit.add(target);
            target.hurtServer(level, source, WeaponLevels.pick(DAMAGE, weaponLevel));
            if (poison > 0) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, poison * 20, WeaponLevels.pick(POISON_AMPLIFIER, weaponLevel)), owner);
            }
            WeaponLevels.reward(owner, ModItems.SWIFTNESS_DAGGER.get(), target);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    // The dash only lives for a moment: it is not saved with the world
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
