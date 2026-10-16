package com.exoticmoss.weaponseffect.entity.custom;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/// Only the look of the Hammer explosion (the damage is done by HammerItem at once):
/// a shockwave that grows from the hit block to the size of the explosion and fades.
/// Drawn in 3D by HammerShockwaveRenderer.
public class HammerShockwaveEntity extends Entity {
    /// How long the shockwave is seen (ticks, 20 = 1 second).
    public static final int DURATION = 14;

    public HammerShockwaveEntity(EntityType<? extends HammerShockwaveEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /// From 0 (just exploded) to 1 (end).
    public float getProgress(float partialTicks) {
        return Math.min(1, (this.tickCount + partialTicks) / DURATION);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount >= DURATION) this.discard();
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
