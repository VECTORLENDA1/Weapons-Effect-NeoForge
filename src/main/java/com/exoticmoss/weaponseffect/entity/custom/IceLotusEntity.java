package com.exoticmoss.weaponseffect.entity.custom;

import java.util.UUID;
import org.jspecify.annotations.Nullable;
import com.exoticmoss.weaponseffect.util.EntityLimit;
import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModItems;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import com.exoticmoss.weaponseffect.util.Freeze;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/// An ice lotus (Ice Sword, level V): grows where a shard from the sky lands. When a mob touches it,
/// it explodes, hurting and freezing the mobs around. Drawn in 3D by IceLotusRenderer.
public class IceLotusEntity extends Entity implements EntityLimit.Owned {
    /// Most of these a player can have at once (more would make the game lag, see EntityLimit).
    /// Over this, the oldest one disappears when a new one is made.
    public static final int MAX_ALIVE = 24;

    /// The explosion when a mob touches the lotus: radius (blocks), damage and freezing (seconds).
    public static final float EXPLOSION_RADIUS = 2.5F;
    public static final float EXPLOSION_DAMAGE = 8.0F;
    public static final int FREEZE_SECONDS = 6;
    /// How long the lotus takes to open (ticks).
    public static final int GROW_TICKS = 10;

    private static final EntityDataAccessor<Integer> DATA_LIFETIME = SynchedEntityData.defineId(IceLotusEntity.class, EntityDataSerializers.INT);

    private LivingEntity owner;
    private int weaponLevel = 1;

    public IceLotusEntity(EntityType<? extends IceLotusEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void grow(ServerLevel level, LivingEntity owner, Vec3 at, int weaponLevel, int seconds) {
        IceLotusEntity lotus = new IceLotusEntity(ModEntities.ICE_LOTUS.get(), level);
        lotus.owner = owner;
        lotus.weaponLevel = weaponLevel;
        lotus.entityData.set(DATA_LIFETIME, seconds * 20);
        lotus.setPos(at.x, at.y, at.z);
        lotus.setYRot(level.getRandom().nextFloat() * 360);
        EntityLimit.spawnReplacingOldest(level, lotus, MAX_ALIVE);
    }

    /// From 0 to 1: how open the lotus is (it opens at the start and closes at the end).
    public float getOpen(float partialTicks) {
        float age = this.tickCount + partialTicks;
        return Mth.clamp(Math.min(age / GROW_TICKS, (this.entityData.get(DATA_LIFETIME) - age) / GROW_TICKS), 0, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.tickCount >= this.entityData.get(DATA_LIFETIME)) {
            this.discard();
            return;
        }
        // A mob touching the lotus makes it explode
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.3))) {
            if (mob != owner && mob.isAlive()) {
                explode(level);
                return;
            }
        }
    }

    private void explode(ServerLevel level) {
        DamageSource source = owner instanceof Player player ? this.damageSources().playerAttack(player) : this.damageSources().freeze();
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(EXPLOSION_RADIUS))) {
            if (mob == owner || mob.position().distanceTo(this.position()) > EXPLOSION_RADIUS + mob.getBbWidth() / 2) continue;
            mob.hurtServer(level, source, EXPLOSION_DAMAGE);
            mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, FREEZE_SECONDS * 20, 3), owner);
            Freeze.apply(mob, FREEZE_SECONDS);
            if (owner != null) WeaponLevels.reward(owner, ModItems.ICE_SWORD.get(), mob);
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 0.7F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.POWDER_SNOW_BREAK, SoundSource.PLAYERS, 1.2F, 0.8F);
        // the 3D look of the explosion: the same shockwave of the Hammer, light blue
        ShockwaveEntity.spawn(level, this.position(), EXPLOSION_RADIUS, 0xA0B0E8FF);
        this.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIFETIME, 200);
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

    @Override
    public @Nullable UUID abilityOwnerId() {
        return owner == null ? null : owner.getUUID();
    }
}
