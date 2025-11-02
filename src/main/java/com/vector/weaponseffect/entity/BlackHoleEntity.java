package com.vector.weaponseffect.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.vector.weaponseffect.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlackHoleEntity extends Entity {
    public static final int DEFAULT_LIFETIME = 200;
    public static final float DEFAULT_MAX_RADIUS = 10.0F;
    private static final float GROWTH_RATE = 0.08F;

    private int lifetime;
    private float currentRadius;
    private float maxRadius;
    private UUID ownerUUID;
    private float size;

    public BlackHoleEntity(EntityType<? extends BlackHoleEntity> entityType, Level level) {
        super(entityType, level);
        this.size = 1.0f;
    }

    public BlackHoleEntity(Level world, double x, double y, double z, int lifetime, float maxRadius, UUID ownerUUID) {
        this(ModEntities.BLACK_HOLE.get(), world);
        this.setPos(x, y, z);
        this.lifetime = lifetime;
        this.maxRadius = maxRadius;
        this.currentRadius = 2.0F;
        this.ownerUUID = ownerUUID;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        if (this.lifetime-- <= 0) {
            this.discard();
            return;
        }

        if (this.currentRadius < this.maxRadius) {
            this.currentRadius = Math.min(this.currentRadius + GROWTH_RATE, this.maxRadius);
        }

        this.setSize(this.currentRadius);

        this.suckBlocksAndEntities();
    }

    public float getCurrentRadius() {
        return this.currentRadius;
    }

    public void suckBlocksAndEntities() {
        int range = (int) Math.ceil(this.currentRadius * 3);
        BlockPos centerPos = this.blockPosition();

        List<BlockPos> blocksToRemove = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(centerPos.offset(-range, -range, -range), centerPos.offset(range, range, range))) {
            double distance = Math.sqrt(pos.distSqr(centerPos));
            if (distance <= this.currentRadius) {
                BlockState state = this.level().getBlockState(pos);
                if (!state.isAir()) {
                    if (isLiquid(state)) {
                        this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    } else if (state.getDestroySpeed(this.level(), pos) >= 0) {
                        this.level().destroyBlock(pos, false);
                    }
                }
            }
        }

        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(range),
                entity -> !(entity instanceof BlackHoleEntity) && !entity.getUUID().equals(this.ownerUUID));

        for (Entity entity : entities) {
            if (entity instanceof Player player) {
                ItemStack itemStack = player.getMainHandItem();
                if (itemStack.getItem() == ModItems.WINGS_OF_DOOM.get()) {
                    continue;
                }
            }

            Vec3 directionToBlackHole = this.position().subtract(entity.position()).normalize();
            double distance = entity.distanceTo(this);

            double innerZoneRadius = this.currentRadius * 1.5;
            double middleZoneRadius = this.currentRadius * 2.25;
            double outerZoneRadius = this.currentRadius * 3.0;

            double strength;

            if (distance <= this.currentRadius) {
                strength = 10.0;//Inside Black Hole
            } else if (distance <= innerZoneRadius) {
                strength = 2.0;// First area
            } else if (distance <= middleZoneRadius) {
                strength = 1.0;// Second area
            } else if (distance <= outerZoneRadius) {
                strength = 0.5;// Third area
            } else {
                continue;//Safe to flee
            }

            Vec3 force = directionToBlackHole.scale(strength * 0.2);
            entity.setDeltaMovement(entity.getDeltaMovement().add(force));
            entity.hurtMarked = true;

            double killRadius = this.currentRadius * 0.6;//Area of killradius insade the Black Hole

            if (distance <= killRadius) {
                if (entity instanceof LivingEntity) {
                    entity.hurt(this.level().damageSources().magic(), 10000.0F);
                } else {
                    entity.discard();
                }
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {}

    public Level getLevel() {
        return this.level();
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getSize() {
        return size;
    }

    private boolean isLiquid(BlockState state) {
        return !state.getFluidState().isEmpty();
    }
}
