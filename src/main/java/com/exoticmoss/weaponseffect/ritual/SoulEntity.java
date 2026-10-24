package com.exoticmoss.weaponseffect.ritual;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/// The soul of a monster killed in a Soul Ritual: it flies from the monster to the floating weapon and then
/// orbits slowly around it (like the items around a Gravity Core) until the ritual ends. At the end of the
/// ritual the orbit gets smaller and the souls go into the weapon.
/// Only for the look (drawn in 3D by SoulRenderer).
public class SoulEntity extends Entity {
    /// How fast the soul flies to the weapon (blocks per tick).
    public static final double SPEED = 0.45;
    /// The orbit: distance from the weapon (blocks, random between the two), how high or low it can go,
    /// and how fast it turns (radians per tick; 0.03 = one turn in about 10 seconds).
    public static final double ORBIT_MIN_RADIUS = 1.2;
    public static final double ORBIT_MAX_RADIUS = 2.0;
    public static final double ORBIT_HEIGHT = 0.6;
    public static final double ORBIT_SPEED = 0.03;
    /// Safety: a soul without a ritual disappears.
    private static final int MAX_FLY_TICKS = 200;

    private SoulRitualEntity ritual;
    private boolean orbiting;
    private double angle, radius, height;

    public SoulEntity(EntityType<? extends SoulEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void spawn(ServerLevel level, Vec3 from, SoulRitualEntity ritual) {
        SoulEntity soul = new SoulEntity(ModEntities.SOUL, level);
        soul.ritual = ritual;
        soul.angle = level.getRandom().nextDouble() * Math.PI * 2;
        soul.radius = ORBIT_MIN_RADIUS + level.getRandom().nextDouble() * (ORBIT_MAX_RADIUS - ORBIT_MIN_RADIUS);
        soul.height = (level.getRandom().nextDouble() * 2 - 1) * ORBIT_HEIGHT;
        soul.setPos(from.x, from.y + 1, from.z);
        // starts going up, then turns to the weapon
        soul.setDeltaMovement(0, SPEED, 0);
        level.addFreshEntity(soul);
        level.playSound(null, from.x, from.y, from.z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /// The place of this soul on its orbit right now.
    private Vec3 orbitPoint() {
        double size = radius * (1 - ritual.getFinishProgress());
        return ritual.weaponPosition().add(Math.cos(angle) * size, height * (1 - ritual.getFinishProgress()), Math.sin(angle) * size);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel)) return;
        if (ritual == null || ritual.isRemoved() || (!orbiting && this.tickCount > MAX_FLY_TICKS)) {
            this.discard();
            return;
        }

        angle += ORBIT_SPEED;
        Vec3 next;
        if (orbiting) {
            next = orbitPoint();
        } else {
            // Flies to its place on the orbit, and starts orbiting when it gets there
            Vec3 toOrbit = orbitPoint().subtract(this.position());
            if (toOrbit.length() < SPEED) {
                orbiting = true;
                next = orbitPoint();
            } else {
                Vec3 direction = this.getDeltaMovement().normalize().lerp(toOrbit.normalize(), 0.2).normalize();
                next = this.position().add(direction.scale(SPEED));
            }
        }
        this.setDeltaMovement(next.subtract(this.position()));
        this.setPos(next);
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

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
