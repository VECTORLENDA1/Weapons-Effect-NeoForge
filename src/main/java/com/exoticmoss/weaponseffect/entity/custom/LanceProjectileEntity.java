package com.exoticmoss.weaponseffect.entity.custom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.item.ModItems;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/// The thrown Lance. Works like a trident (also with Loyalty), but does more damage.
/// Ability "Rain of Lances": the first time the thrown lance hits something (a mob or a block),
/// lances fall from the sky around that place. Those falling lances can't be picked up and
/// disappear a moment after landing.
public class LanceProjectileEntity extends AbstractArrow {
    // ---------------------------------------------------------------- Rain of Lances
    /// How many lances fall.
    public static final int RAIN_COUNT = 12;
    /// Damage of each falling lance.
    public static final float RAIN_DAMAGE = 6.0F;
    /// Size of the rain: radius around the hit (blocks) and how high the lances start (blocks above the hit).
    public static final float RAIN_RADIUS = 4.0F;
    public static final float RAIN_HEIGHT = 12.0F;
    /// How fast the lances fall (blocks per tick) and how long they stay in the ground before disappearing (ticks).
    public static final float RAIN_SPEED = 2.0F;
    public static final int RAIN_LANCE_LIFETIME = 30;

    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(LanceProjectileEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(LanceProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    /// Damage done when the lance hits a mob.
    private static final float DAMAGE = 10.0F;
    private boolean dealtDamage;
    /// true for the lances of the rain (they don't make another rain).
    private boolean isRainLance;
    /// true after this lance already made its rain.
    private boolean rainDone;
    public int clientSideReturnTridentTickCount;

    public LanceProjectileEntity(EntityType<? extends LanceProjectileEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public LanceProjectileEntity(Level pLevel, LivingEntity pShooter, ItemStack pPickupItemStack) {
        super(ModEntities.LANCE.get(), pShooter, pLevel, pPickupItemStack, null);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(pPickupItemStack));
        this.entityData.set(ID_FOIL, pPickupItemStack.hasFoil());
    }

    public LanceProjectileEntity(Level pLevel, double pX, double pY, double pZ, ItemStack pPickupItemStack) {
        super(ModEntities.LANCE.get(), pX, pY, pZ, pLevel, pPickupItemStack, pPickupItemStack);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(pPickupItemStack));
        this.entityData.set(ID_FOIL, pPickupItemStack.hasFoil());
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(ID_LOYALTY, (byte)0);
        pBuilder.define(ID_FOIL, false);
    }

    /// The ability: lances fall from the sky around this place.
    private void startRain(ServerLevel level, Vec3 center) {
        rainDone = true;
        for (int i = 0; i < RAIN_COUNT; i++) {
            // A random place inside the circle, and a random height so they don't all land at once
            double angle = level.getRandom().nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(level.getRandom().nextDouble()) * RAIN_RADIUS;
            double height = RAIN_HEIGHT + level.getRandom().nextDouble() * 6;
            LanceProjectileEntity lance = new LanceProjectileEntity(level,
                    center.x + Math.cos(angle) * distance, center.y + height, center.z + Math.sin(angle) * distance,
                    new ItemStack(ModItems.LANCE.get()));
            lance.isRainLance = true;
            lance.setOwner(this.getOwner());
            lance.pickup = Pickup.DISALLOWED;
            lance.shoot(0, -1, 0, RAIN_SPEED, 2.0F);
            level.addFreshEntity(lance);
        }
        level.playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    @Override
    public void tick() {
        if (isRainLance && this.inGroundTime > RAIN_LANCE_LIFETIME) {
            this.discard();
            return;
        }
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity owner = this.getOwner();
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (loyalty > 0 && (this.dealtDamage || this.isNoPhysics()) && owner != null) {
            if (!this.isAcceptibleReturnOwner()) {
                if (this.level() instanceof ServerLevel serverLevel && this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(serverLevel, this.getPickupItem(), 0.1F);
                }

                this.discard();
            } else {
                if (!(owner instanceof Player) && this.position().distanceTo(owner.getEyePosition()) < (double)owner.getBbWidth() + 1.0) {
                    this.discard();
                    return;
                }

                this.setNoPhysics(true);
                Vec3 vec3 = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015 * (double)loyalty, this.getZ());
                double accel = 0.05 * (double)loyalty;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(vec3.normalize().scale(accel)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                this.clientSideReturnTridentTickCount++;
            }
        }

        super.tick();
    }

    private boolean isAcceptibleReturnOwner() {
        Entity owner = this.getOwner();
        if (owner != null && owner.isAlive()) {
            return !(owner instanceof ServerPlayer) || !owner.isSpectator();
        }
        return false;
    }

    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    @Override
    protected @Nullable EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
        return this.dealtDamage ? null : super.findHitEntity(pStartVec, pEndVec);
    }

    @Override
    protected Collection<EntityHitResult> findHitEntities(Vec3 pStartVec, Vec3 pEndVec) {
        EntityHitResult hit = this.findHitEntity(pStartVec, pEndVec);
        return hit != null ? List.of(hit) : List.of();
    }

    // The falling lances never hit the player who threw the lance
    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(isRainLance && entity == this.getOwner()) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!isRainLance && !rainDone && this.level() instanceof ServerLevel level) startRain(level, hitResult.getLocation());
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        if (!isRainLance && !rainDone && this.level() instanceof ServerLevel level) startRain(level, entity.position());
        float damage = isRainLance ? RAIN_DAMAGE : DAMAGE;
        Entity owner = this.getOwner();
        DamageSource damagesource = this.damageSources().trident(this, owner == null ? this : owner);
        if (this.level() instanceof ServerLevel serverlevel) {
            damage = EnchantmentHelper.modifyDamage(serverlevel, this.getWeaponItem(), entity, damagesource, damage);
        }

        this.dealtDamage = true;
        if (entity.hurtOrSimulate(damagesource, damage)) {
            if (entity.is(EntityTypes.ENDERMAN)) {
                return;
            }

            if (this.level() instanceof ServerLevel serverlevel) {
                EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak(serverlevel, entity, damagesource, this.getWeaponItem(), weapon -> this.kill(serverlevel));
            }

            if (entity instanceof LivingEntity livingentity) {
                this.doKnockback(livingentity, damagesource);
                this.doPostHurtEffects(livingentity);
            }
        }

        this.deflect(ProjectileDeflection.REVERSE, entity, this.owner, false);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void hitBlockEnchantmentEffects(ServerLevel pLevel, BlockHitResult pHitResult, ItemStack pStack) {
        Vec3 vec3 = pHitResult.getBlockPos().clampLocationWithin(pHitResult.getLocation());
        EnchantmentHelper.onHitBlock(
                pLevel,
                pStack,
                this.getOwner() instanceof LivingEntity livingentity ? livingentity : null,
                this,
                null,
                vec3,
                pLevel.getBlockState(pHitResult.getBlockPos()),
                item -> this.kill(pLevel)
        );
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    @Override
    protected boolean tryPickup(Player pPlayer) {
        return super.tryPickup(pPlayer) || this.isNoPhysics() && this.ownedBy(pPlayer) && pPlayer.getInventory().add(this.getPickupItem());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.LANCE.get());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player pEntity) {
        if (this.ownedBy(pEntity) || this.getOwner() == null) {
            super.playerTouch(pEntity);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput pInput) {
        super.readAdditionalSaveData(pInput);
        this.dealtDamage = pInput.getBooleanOr("DealtDamage", false);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(this.getPickupItemStackOrigin()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput pOutput) {
        super.addAdditionalSaveData(pOutput);
        pOutput.putBoolean("DealtDamage", this.dealtDamage);
    }

    private byte getLoyaltyFromItem(ItemStack pStack) {
        return this.level() instanceof ServerLevel serverlevel ? (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverlevel, pStack, this), 0, 127) : 0;
    }

    @Override
    public void tickDespawn() {
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (this.pickup != Pickup.ALLOWED || loyalty <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }
}
