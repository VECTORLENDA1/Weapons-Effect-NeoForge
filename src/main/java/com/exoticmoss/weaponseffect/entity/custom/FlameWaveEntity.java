package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/// The "lava tsunami" of the Fire Sword ability: a curved wave of fire and lava that comes out of the player
/// and travels forward, getting wider, burning and hurting every mob it passes through.
/// The entity stays where the player was; the wave is drawn by FlameWaveRenderer (no particles).
public class FlameWaveEntity extends Entity {
    /// How far the wave goes (in blocks).
    public static final float LENGTH = 10.0F;
    /// How many blocks the wave moves each tick (20 ticks = 1 second).
    public static final float SPEED = 0.6F;
    /// Width of the wave when it comes out of the player and at the end (in blocks).
    public static final float START_WIDTH = 1.5F;
    public static final float END_WIDTH = 6.0F;
    /// Height of the tsunami at its biggest (in blocks).
    public static final float HEIGHT = 2.6F;
    /// How much the wave is curved (the middle goes ahead of the sides), in blocks.
    public static final float CURVE = 0.8F;
    /// Damage done to each mob the wave hits (only once per wave) and how long it burns (seconds).
    public static final float DAMAGE = 13.0F;
    public static final int FIRE_SECONDS = 5;

    private LivingEntity owner;
    private final Set<LivingEntity> hit = new HashSet<>();

    public FlameWaveEntity(EntityType<? extends FlameWaveEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public FlameWaveEntity(Level level, LivingEntity owner) {
        this(ModEntities.FLAME_WAVE.get(), level);
        this.owner = owner;
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        this.setYRot(owner.getYRot());
    }

    /// Starts a wave of flames in front of the attacker.
    public static void launch(ServerLevel level, LivingEntity owner) {
        level.addFreshEntity(new FlameWaveEntity(level, owner));
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.6F);
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 4.0F, 1.0F);
    }

    /// How far the front of the wave is from where it started (same on the server and the client).
    public float getDistance(float partialTicks) {
        return 0.5F + SPEED * (this.tickCount + partialTicks);
    }

    /// From 0 (start) to 1 (end of the way).
    public float getProgress(float partialTicks) {
        return Mth.clamp(getDistance(partialTicks) / LENGTH, 0, 1);
    }

    public float getWidth(float partialTicks) {
        return Mth.lerp(getProgress(partialTicks), START_WIDTH, END_WIDTH);
    }

    /// The direction the wave travels (flat, no up or down).
    public Vec3 getForward() {
        return Vec3.directionFromRotation(0, this.getYRot());
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        if (owner == null || !owner.isAlive() || getDistance(0) > LENGTH) {
            this.discard();
            return;
        }
        hurtMobs(level);
    }

    /// Damage: every mob at the front of the wave (each one only once).
    private void hurtMobs(ServerLevel level) {
        float distance = getDistance(0);
        float half = getWidth(0) / 2;
        Vec3 forward = getForward();
        Vec3 center = this.position().add(forward.scale(distance));
        AABB area = new AABB(center, center).inflate(half + 0.5, 0, half + 0.5).expandTowards(0, HEIGHT + 0.5, 0).move(0, -0.5, 0);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == owner || target.fireImmune() || hit.contains(target)) continue;
            // Only the mobs really close to the line of the wave (not the corners of the box)
            double along = target.position().subtract(this.position()).dot(forward);
            if (Math.abs(along - distance) > 1.5) continue;

            hit.add(target);
            target.setRemainingFireTicks(FIRE_SECONDS * 20);
            target.hurtServer(level, damageSource(owner), DAMAGE);
        }
    }

    private static DamageSource damageSource(LivingEntity attacker) {
        return attacker instanceof Player player
                ? player.damageSources().playerAttack(player)
                : attacker.damageSources().mobAttack(attacker);
    }

    /// The height of the ground at this place (looks 2 blocks up and 3 down from the wave's height).
    /// Used by the renderer so the flames go up and down the steps. NaN = inside a wall.
    public double findGround(double x, double z) {
        Level level = this.level();
        BlockPos start = BlockPos.containing(x, this.getY() + 0.5, z);
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos up = start.above(dy);
            if (level.getBlockState(up).getCollisionShape(level, up).isEmpty()) {
                for (int down = 0; down <= 3; down++) {
                    BlockPos below = up.below(down + 1);
                    if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                        double top = level.getBlockState(below).getCollisionShape(level, below).max(net.minecraft.core.Direction.Axis.Y);
                        return below.getY() + top;
                    }
                }
                return up.getY();
            }
        }
        return Double.NaN;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    // The wave only lives for a moment: it is not saved with the world
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
