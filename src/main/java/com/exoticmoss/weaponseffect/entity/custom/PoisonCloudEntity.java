package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.util.Aim;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/// The Poison Sword ability ("Poison Cloud"): a cloud of poison where the player is aiming.
/// It grows, stays for a while and fades away, poisoning every mob inside it.
/// Drawn in 3D by PoisonCloudRenderer.
public class PoisonCloudEntity extends Entity {
    /// Size of the cloud (blocks): radius and height.
    public static final float RADIUS = 4.0F;
    public static final float HEIGHT = 2.5F;
    /// How long the cloud lasts (ticks, 20 = 1 second), how long it takes to grow and to fade at the end.
    public static final int DURATION = 200;
    public static final int GROW_TICKS = 20;
    public static final int FADE_TICKS = 30;
    /// The poison given to the mobs inside: seconds and level (0 = Poison I, 1 = Poison II...).
    public static final int POISON_SECONDS = 4;
    public static final int POISON_AMPLIFIER = 1;
    /// How far the player can aim the cloud (blocks).
    public static final float RANGE = 20.0F;

    private LivingEntity owner;

    public PoisonCloudEntity(EntityType<? extends PoisonCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /// The ability: a poison cloud where the player is aiming.
    public static void launch(ServerLevel level, LivingEntity owner) {
        Vec3 target = Aim.target(owner, RANGE);
        PoisonCloudEntity cloud = new PoisonCloudEntity(ModEntities.POISON_CLOUD.get(), level);
        cloud.owner = owner;
        cloud.setPos(target.x, target.y, target.z);
        level.addFreshEntity(cloud);
        level.playSound(null, target.x, target.y, target.z, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.5F, 0.6F);
    }

    /// Size of the cloud right now, from 0 to 1 (grows at the start, shrinks at the end).
    public float getSize(float partialTicks) {
        float age = this.tickCount + partialTicks;
        float grow = Mth.clamp(age / GROW_TICKS, 0, 1);
        float fade = Mth.clamp((DURATION - age) / FADE_TICKS, 0, 1);
        return Math.min(grow, fade);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.tickCount >= DURATION) {
            this.discard();
            return;
        }
        // Poisons the mobs inside every half second
        if (this.tickCount % 10 == 0) poisonMobsInside(level);
    }

    private void poisonMobsInside(ServerLevel level) {
        float radius = RADIUS * getSize(0);
        AABB area = new AABB(this.getX() - radius, this.getY(), this.getZ() - radius,
                this.getX() + radius, this.getY() + HEIGHT, this.getZ() + radius);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (mob == owner || mob.position().subtract(this.position()).horizontalDistance() > radius) continue;
            mob.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_SECONDS * 20, POISON_AMPLIFIER), owner);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
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
