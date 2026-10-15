package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
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

import java.util.HashSet;
import java.util.Set;

/// The "wave of flames" of the Fire Sword ability: several ribbons of fire and lava twisting around
/// each other, shot from the sword to where the player is aiming. It spreads out a bit as it goes,
/// stops at walls, burns and hurts every mob it touches.
/// The entity stays at the sword; the flames are drawn by FlameWaveRenderer (no particles).
public class FlameWaveEntity extends Entity {
    /// How far the flames reach, measured from the player (in blocks). They stop earlier if they hit a wall.
    public static final float LENGTH = 8.0F;
    /// How many blocks the front of the flames moves each tick (20 ticks = 1 second).
    public static final float SPEED = 0.6F;
    /// How many ticks the flames stay at full length (connected to the sword) before the end of them
    /// leaves the sword. The animation always lasts the whole ability: the flames come out until they
    /// reach their full length (LENGTH or a wall), stay there HOLD ticks, and only then the end leaves the sword.
    public static final int HOLD = 4;
    /// How many ribbons of fire twist around each other.
    public static final int RIBBONS = 10;
    /// Total width of the flames (in blocks, from one side to the other) at the sword and at the end.
    /// The height is WIDTH x FLATNESS (see FlameWaveRenderer). Mobs touching this width are hit.
    public static final float START_WIDTH = 0.3F;
    public static final float WIDTH = 4.0F;
    /// How far in front of the player's eyes the flames start (the sword), in blocks.
    public static final float START_DISTANCE = 0.5F;
    /// Damage done to each mob the flames hit (only once) and how long it burns (seconds).
    public static final float DAMAGE = 8.0F;
    public static final int FIRE_SECONDS = 5;

    private LivingEntity owner;
    private final Set<LivingEntity> hit = new HashSet<>();
    /// Where the flames stop (a wall or LENGTH). Calculated once, on the server and on the client.
    private float maxLength = -1;

    public FlameWaveEntity(EntityType<? extends FlameWaveEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public FlameWaveEntity(Level level, LivingEntity owner) {
        this(ModEntities.FLAME_WAVE.get(), level);
        this.owner = owner;
        // Comes out of the sword, a bit in front of the player, in the direction the player is aiming
        Vec3 look = owner.getLookAngle();
        Vec3 start = owner.getEyePosition().add(0, -0.35, 0).add(look.scale(START_DISTANCE));
        this.setPos(start.x, start.y, start.z);
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
    }

    /// Shoots the flames to where the attacker is aiming.
    public static void launch(ServerLevel level, LivingEntity owner) {
        level.addFreshEntity(new FlameWaveEntity(level, owner));
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.6F);
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 4.0F, 1.0F);
    }

    /// The direction of the flames (where the player was aiming).
    public Vec3 getFlameDirection() {
        return Vec3.directionFromRotation(this.getXRot(), this.getYRot());
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
    /// It only starts moving after the front reached the full length and HOLD ticks passed.
    public float getTail(float partialTicks) {
        float max = getMaxLength();
        return Mth.clamp(SPEED * (this.tickCount + partialTicks - HOLD) - max, 0, max);
    }

    /// The length of the flames from the sword (LENGTH is counted from the player).
    public static float reach() {
        return Math.max(0.5F, LENGTH - START_DISTANCE);
    }

    /// Half of the width of the flames at this distance from the sword (from the middle line to the outer edge).
    public static float halfWidthAt(float distance) {
        return Mth.lerp(Mth.clamp(distance / reach(), 0, 1), START_WIDTH, WIDTH) / 2;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        if (owner == null || !owner.isAlive() || getTail(0) >= getMaxLength()) {
            this.discard();
            return;
        }
        hurtMobs(level);
    }

    /// Damage: every mob touching the flames (each one only once).
    private void hurtMobs(ServerLevel level) {
        float head = getHead(0);
        float tail = getTail(0);
        Vec3 start = this.position();
        Vec3 direction = getFlameDirection();
        AABB area = new AABB(start.add(direction.scale(tail)), start.add(direction.scale(head))).inflate(WIDTH / 2 + 1);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == owner || target.fireImmune() || hit.contains(target)) continue;

            // Distance along the flames and distance from the middle line
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(start);
            double along = toTarget.dot(direction);
            // The mob is hit when its body touches the flames (half of its width counts)
            double body = target.getBbWidth() / 2;
            if (along < tail - body || along > head + body) continue;
            double fromLine = toTarget.subtract(direction.scale(along)).length();
            if (fromLine > halfWidthAt((float) along) + body) continue;

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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
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
