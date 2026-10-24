package com.exoticmoss.weaponseffect.entity.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModItems;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import com.exoticmoss.weaponseffect.util.Aim;
import com.exoticmoss.weaponseffect.util.Freeze;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/// One ice shard of the Ice Sword ability ("Ice Shard"). The shards appear in an arc above the player,
/// float there for a moment and then fly, one after the other, to where the player was aiming.
/// A hit freezes the mob (the freezing of powder snow) and makes it slow.
///  - Level III and up: each shard also freezes the mobs around the place it hits.
///  - Level V: more shards fall from the sky over the target; where they land, ice lotus grow
///    (they explode and freeze when a mob touches them, see IceLotusEntity).
/// Drawn in 3D by IceShardRenderer.
///
/// TABLES: one number for each level { I, II, III, IV, V }.
public class IceShardEntity extends Projectile {
    /// How many shards appear above the player.
    public static final int[] SHARD_COUNT = {8, 10, 12, 14, 16};
    /// Damage of each shard.
    public static final float[] DAMAGE = {6, 7, 8, 9, 10};
    /// How long a hit mob stays frozen (seconds), and the level of its Slowness (0 = I, 2 = III).
    public static final int[] FREEZE_SECONDS = {4, 5, 6, 7, 8};
    public static final int[] SLOWNESS_AMPLIFIER = {2, 2, 3, 3, 4};
    /// Mobs around the hit that also get frozen: radius in blocks (0 = only the mob hit).
    public static final float[] AREA_FREEZE_RADIUS = {0, 0, 2, 2.5F, 3};
    /// Shards that fall from the sky over the target (0 = none), and how long their ice lotus last (seconds, 0 = none).
    public static final int[] SKY_SHARDS = {0, 0, 0, 0, 16};
    public static final int[] LOTUS_SECONDS = {0, 0, 0, 0, 10};

    /// How fast the shards fly (blocks per tick, 20 ticks = 1 second) and how far the player can aim (blocks).
    public static final float SPEED = 1.6F;
    public static final float RANGE = 40.0F;
    /// Ticks the shards float before the first one flies, and ticks between one shard and the next.
    public static final int HOVER_TICKS = 12;
    public static final int LAUNCH_INTERVAL = 2;
    /// The arc above the player: height above the eyes and width (blocks).
    public static final float ARC_HEIGHT = 1.4F;
    public static final float ARC_WIDTH = 3.0F;
    /// The shards from the sky: how wide the area is (radius) and how high they start (blocks).
    public static final float SKY_RADIUS = 4.0F;
    public static final float SKY_HEIGHT = 14.0F;
    /// A shard that hits nothing disappears after this many ticks of flight.
    private static final int MAX_FLIGHT_TICKS = 80;

    private int slot;
    private int weaponLevel = 1;
    private Vec3 target = Vec3.ZERO;
    private boolean flying;
    /// true for the shards that fall from the sky (level V).
    private boolean fromSky;
    private int flightTicks;

    public IceShardEntity(EntityType<? extends IceShardEntity> type, Level level) {
        super(type, level);
    }

    private IceShardEntity(Level level, LivingEntity owner, int weaponLevel) {
        this(ModEntities.ICE_SHARD, level);
        this.setOwner(owner);
        this.weaponLevel = weaponLevel;
    }

    /// The ability: the shards above the player, aimed at what the player is looking at (and the sky shards at level V).
    public static void launch(ServerLevel level, LivingEntity owner, int weaponLevel) {
        Vec3 target = Aim.target(owner, RANGE);
        for (int i = 0; i < WeaponLevels.pick(SHARD_COUNT, weaponLevel); i++) {
            IceShardEntity shard = new IceShardEntity(level, owner, weaponLevel);
            shard.slot = i;
            shard.target = target;
            shard.hover(owner);
            level.addFreshEntity(shard);
        }
        // Level V: shards falling from the sky over the target (at different heights, so they don't land at once)
        for (int i = 0; i < WeaponLevels.pick(SKY_SHARDS, weaponLevel); i++) {
            IceShardEntity shard = new IceShardEntity(level, owner, weaponLevel);
            double angle = level.getRandom().nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(level.getRandom().nextDouble()) * SKY_RADIUS;
            double height = SKY_HEIGHT + level.getRandom().nextDouble() * 8;
            shard.setPos(target.x + Math.cos(angle) * distance, target.y + height, target.z + Math.sin(angle) * distance);
            shard.fromSky = true;
            shard.flying = true;
            shard.setDeltaMovement(0, -SPEED, 0);
            shard.setXRot(-90);
            level.addFreshEntity(shard);
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
        if (++flightTicks > MAX_FLIGHT_TICKS) shatter(level, this.position());
    }

    /// Floats in its place of the arc above the player's head, pointing at the target.
    private void hover(LivingEntity owner) {
        int count = WeaponLevels.pick(SHARD_COUNT, weaponLevel);
        float side = count <= 1 ? 0 : slot / (float) (count - 1) * 2 - 1; // -1 left ... 1 right
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
        return entity != this.getOwner() && !(entity instanceof IceShardEntity) && !(entity instanceof IceLotusEntity) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!(this.level() instanceof ServerLevel level)) return;
        Entity owner = this.getOwner();
        var source = owner instanceof Player player ? this.damageSources().playerAttack(player) : this.damageSources().freeze();
        if (hitResult.getEntity().hurtServer(level, source, WeaponLevels.pick(DAMAGE, weaponLevel)) && hitResult.getEntity() instanceof LivingEntity mob) {
            freeze(mob);
        }
        shatter(level, hitResult.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        if (!(this.level() instanceof ServerLevel level)) return;
        // A sky shard that lands on the top of a block grows an ice lotus there (level V)
        int lotusSeconds = WeaponLevels.pick(LOTUS_SECONDS, weaponLevel);
        if (fromSky && lotusSeconds > 0 && hitResult.getDirection() == Direction.UP && this.getOwner() instanceof LivingEntity owner) {
            IceLotusEntity.grow(level, owner, hitResult.getLocation(), weaponLevel, lotusSeconds);
        }
        shatter(level, hitResult.getLocation());
    }

    /// Freezes and slows a mob, and gives experience to the Ice Sword.
    private void freeze(LivingEntity mob) {
        mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, WeaponLevels.pick(FREEZE_SECONDS, weaponLevel) * 20,
                WeaponLevels.pick(SLOWNESS_AMPLIFIER, weaponLevel)), this.getOwner());
        Freeze.apply(mob, WeaponLevels.pick(FREEZE_SECONDS, weaponLevel));
        if (this.getOwner() instanceof LivingEntity owner) WeaponLevels.reward(owner, ModItems.ICE_SWORD, mob);
    }

    /// The shard breaks; at level III and up it also freezes the mobs around.
    private void shatter(ServerLevel level, Vec3 at) {
        float radius = WeaponLevels.pick(AREA_FREEZE_RADIUS, weaponLevel);
        if (radius > 0) {
            for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius))) {
                if (mob != this.getOwner() && mob.position().distanceTo(at) <= radius + mob.getBbWidth() / 2) freeze(mob);
            }
        }
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.7F, 1.4F);
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
