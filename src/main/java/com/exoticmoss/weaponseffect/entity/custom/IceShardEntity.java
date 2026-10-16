package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.util.Aim;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/// One ice shard of the Ice Sword ability. The shards appear in an arc above the player, float there
/// for a moment and then fly, one after the other, to where the player was aiming.
/// Drawn in 3D by IceShardRenderer.
public class IceShardEntity extends Projectile {
    /// How many shards appear each time the ability is used.
    public static final int SHARD_COUNT = 8;
    /// Damage of each shard.
    public static final float DAMAGE = 6.0F;
    /// How long a hit mob stays frozen and slow (seconds), and the level of the Slowness (0 = I, 2 = III).
    public static final int FREEZE_SECONDS = 4;
    public static final int SLOWNESS_AMPLIFIER = 2;
    /// How fast the shards fly (blocks per tick, 20 ticks = 1 second) and how far the player can aim (blocks).
    public static final float SPEED = 1.6F;
    public static final float RANGE = 40.0F;
    /// Ticks the shards float before the first one flies, and ticks between one shard and the next.
    public static final int HOVER_TICKS = 12;
    public static final int LAUNCH_INTERVAL = 2;
    /// The arc above the player: height above the eyes and width (blocks).
    public static final float ARC_HEIGHT = 1.4F;
    public static final float ARC_WIDTH = 3.0F;
    /// A shard that hits nothing disappears after this many ticks of flight.
    private static final int MAX_FLIGHT_TICKS = 60;

    private int slot;
    private Vec3 target = Vec3.ZERO;
    private boolean flying;
    private int flightTicks;

    public IceShardEntity(EntityType<? extends IceShardEntity> type, Level level) {
        super(type, level);
    }

    private IceShardEntity(Level level, LivingEntity owner, int slot, Vec3 target) {
        this(ModEntities.ICE_SHARD.get(), level);
        this.setOwner(owner);
        this.slot = slot;
        this.target = target;
        hover(owner);
    }

    /// The ability: creates all the shards above the player, aimed at what the player is looking at.
    public static void launch(ServerLevel level, LivingEntity owner) {
        Vec3 target = Aim.target(owner, RANGE);
        for (int i = 0; i < SHARD_COUNT; i++) {
            level.addFreshEntity(new IceShardEntity(level, owner, i, target));
        }
        level.playSound(null, owner.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.4F);
    }

    @Override
    public void tick() {
        super.tick();
        // Only the server moves the shards; the client gets their position and rotation every tick
        if (!(this.level() instanceof ServerLevel level)) return;

        if (!flying) {
            if (!(this.getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
                this.discard();
                return;
            }
            hover(owner);
            if (this.tickCount >= HOVER_TICKS + slot * LAUNCH_INTERVAL) {
                flying = true;
                this.setDeltaMovement(target.subtract(this.position()).normalize().scale(SPEED));
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.6F, 1.6F);
            }
            return;
        }

        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(hit);
            return;
        }
        this.setPos(this.position().add(this.getDeltaMovement()));
        this.updateRotation();
        if (++flightTicks > MAX_FLIGHT_TICKS) shatter(level);
    }

    /// Floats in its place of the arc above the player's head, pointing at the target.
    private void hover(LivingEntity owner) {
        float side = SHARD_COUNT <= 1 ? 0 : slot / (float) (SHARD_COUNT - 1) * 2 - 1; // -1 left ... 1 right
        float yaw = owner.getYRot() * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0, -Mth.sin(yaw));
        double height = ARC_HEIGHT + 0.5 * (1 - side * side)            // higher in the middle
                + 0.08 * Mth.sin((this.tickCount + slot * 5) * 0.3F);  // small up and down movement
        Vec3 home = owner.getEyePosition().add(right.scale(side * ARC_WIDTH / 2)).add(0, height, 0);
        this.setPos(home.x, home.y, home.z);

        // Point at the target
        Vec3 dir = target.subtract(home);
        this.setYRot((float) (Mth.atan2(dir.x, dir.z) * Mth.RAD_TO_DEG));
        this.setXRot((float) (Mth.atan2(dir.y, dir.horizontalDistance()) * Mth.RAD_TO_DEG));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity != this.getOwner() && !(entity instanceof IceShardEntity) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!(this.level() instanceof ServerLevel level)) return;
        Entity owner = this.getOwner();
        var source = owner instanceof Player player ? this.damageSources().playerAttack(player) : this.damageSources().freeze();
        if (hitResult.getEntity().hurtServer(level, source, DAMAGE) && hitResult.getEntity() instanceof LivingEntity mob) {
            mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, FREEZE_SECONDS * 20, SLOWNESS_AMPLIFIER), owner);
            mob.setTicksFrozen(mob.getTicksRequiredToFreeze() + FREEZE_SECONDS * 20);
        }
        shatter(level);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        if (this.level() instanceof ServerLevel level) shatter(level);
    }

    private void shatter(ServerLevel level) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.7F, 1.4F);
        this.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    // The shards only live for a moment: they are not saved with the world
    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
