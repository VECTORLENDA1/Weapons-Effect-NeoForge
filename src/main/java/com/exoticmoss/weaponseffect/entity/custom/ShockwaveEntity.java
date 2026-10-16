package com.exoticmoss.weaponseffect.entity.custom;

import java.util.UUID;
import com.exoticmoss.weaponseffect.util.EntityLimit;
import com.exoticmoss.weaponseffect.entity.ModEntities;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/// A shockwave: a dome of air that grows to its radius and fades (drawn in 3D by ShockwaveRenderer).
/// Used by the explosions (Hammer, giant lance, ice lotus...). It can also do the damage itself
/// (a "blast"), even after a delay (the second explosion of the Hammer at level V).
/// Every mob touching the sphere of the blast takes the SAME damage, close or far from the middle.
public class ShockwaveEntity extends Entity implements EntityLimit.Owned {
    /// Most of these a player can have at once (more would make the game lag, see EntityLimit).
    /// Over this, new explosions still do their damage but are not drawn.
    public static final int MAX_ALIVE = 16;

    /// How long the shockwave is seen (ticks, 20 = 1 second).
    public static final int DURATION = 14;

    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(ShockwaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(ShockwaveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DELAY = SynchedEntityData.defineId(ShockwaveEntity.class, EntityDataSerializers.INT);

    /// What the blast does (null = only the look).
    public record Blast(LivingEntity owner, @Nullable Item weapon, float damage, double knockback, double knockUp,
                        int stunSeconds) {
    }

    private @Nullable Blast blast;

    public ShockwaveEntity(EntityType<? extends ShockwaveEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /// Only the look of an explosion (the damage is done by someone else).
    public static void spawn(ServerLevel level, Vec3 at, float radius, int color) {
        blast(level, at, radius, color, 0, null);
    }

    /// An explosion that also does damage, after "delay" ticks (0 = now).
    public static void blast(ServerLevel level, Vec3 at, float radius, int color, int delay, @Nullable Blast blast) {
        ShockwaveEntity wave = new ShockwaveEntity(ModEntities.SHOCKWAVE.get(), level);
        wave.entityData.set(DATA_RADIUS, radius);
        wave.entityData.set(DATA_COLOR, color);
        wave.entityData.set(DATA_DELAY, delay);
        wave.blast = blast;
        wave.setPos(at.x, at.y, at.z);
        if (delay == 0 && EntityLimit.isFull(level, wave, MAX_ALIVE)) {
            wave.explode(level);   // only the damage, without the look
            return;
        }
        level.addFreshEntity(wave);
        if (delay == 0) wave.explode(level);
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }

    /// From 0 (just exploded) to 1 (end). Negative while waiting for the delay.
    public float getProgress(float partialTicks) {
        return Math.min(1, (this.tickCount + partialTicks - this.entityData.get(DATA_DELAY)) / DURATION);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        int delay = this.entityData.get(DATA_DELAY);
        if (delay > 0 && this.tickCount == delay) explode(level);
        if (this.tickCount >= delay + DURATION) this.discard();
    }

    /// The damage of the blast (if it has one) and the sound.
    private void explode(ServerLevel level) {
        Vec3 center = this.position();
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.7F);
        if (blast == null) return;
        level.playSound(null, center.x, center.y, center.z, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 1.2F, 0.8F);

        float radius = getRadius();
        DamageSource source = blast.owner() instanceof Player player ? player.damageSources().playerAttack(player) : this.damageSources().explosion(this, blast.owner());
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius), mob -> mob != blast.owner())) {
            // Every mob touching the sphere gets the full damage
            if (closestPoint(mob.getBoundingBox(), center).distanceTo(center) > radius) continue;

            mob.hurtServer(level, source, blast.damage());
            if (blast.stunSeconds() > 0) {
                mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, blast.stunSeconds() * 20, 10), blast.owner());
            }
            if (blast.knockback() > 0 || blast.knockUp() > 0) {
                Vec3 away = mob.position().subtract(center).multiply(1, 0, 1).normalize().scale(blast.knockback());
                mob.push(away.x, blast.knockUp(), away.z);
                mob.hurtMarked = true;
            }
            if (blast.weapon() != null) WeaponLevels.reward(blast.owner(), blast.weapon(), mob);
        }
    }

    /// The point of the box nearest to the center (to know if a big mob touches the blast).
    private static Vec3 closestPoint(AABB box, Vec3 point) {
        return new Vec3(Math.clamp(point.x, box.minX, box.maxX), Math.clamp(point.y, box.minY, box.maxY), Math.clamp(point.z, box.minZ, box.maxZ));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, 1.0F);
        builder.define(DATA_COLOR, 0x60E8F0FF);
        builder.define(DATA_DELAY, 0);
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
        return blast == null ? null : blast.owner().getUUID();
    }
}
