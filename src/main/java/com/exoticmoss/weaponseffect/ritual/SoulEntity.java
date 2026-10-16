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

/// The soul of a monster killed in a Soul Ritual: it flies from the monster to the floating weapon.
/// Only for the look (drawn in 3D by SoulRenderer).
public class SoulEntity extends Entity {
    /// How fast the soul flies (blocks per tick) and how much it curves.
    public static final double SPEED = 0.45;
    private static final int MAX_TICKS = 200;

    private Vec3 target = Vec3.ZERO;

    public SoulEntity(EntityType<? extends SoulEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void spawn(ServerLevel level, Vec3 from, Vec3 to) {
        SoulEntity soul = new SoulEntity(ModEntities.SOUL.get(), level);
        soul.target = to;
        soul.setPos(from.x, from.y + 1, from.z);
        // starts going up, then turns to the weapon
        soul.setDeltaMovement(0, SPEED, 0);
        level.addFreshEntity(soul);
        level.playSound(null, from.x, from.y, from.z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel)) return;
        Vec3 toTarget = target.subtract(this.position());
        if (toTarget.length() < 0.6 || this.tickCount > MAX_TICKS) {
            this.discard();
            return;
        }
        Vec3 direction = this.getDeltaMovement().normalize().lerp(toTarget.normalize(), 0.2).normalize();
        this.setDeltaMovement(direction.scale(SPEED));
        this.setPos(this.position().add(this.getDeltaMovement()));
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
