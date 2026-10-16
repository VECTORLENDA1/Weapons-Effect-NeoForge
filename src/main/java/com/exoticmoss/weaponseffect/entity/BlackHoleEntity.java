package com.exoticmoss.weaponseffect.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import com.exoticmoss.weaponseffect.item.ModItems;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlackHoleEntity extends Entity {
    public static final int DEFAULT_LIFETIME = 200;
    public static final float DEFAULT_MAX_RADIUS = 10.0F;


    private static final float MIN_RADIUS = 0.5F;
    private static final float GROWTH_RATE = 0.08F;
    private static final float SHRINK_RATE = 0.08F;
    private static final int DEFAULT_HOLD_TICKS = 60; // 3 segundos a 20 tps


    // Raio sincronizado entre servidor e cliente — ESSENCIAL para o renderer ver o crescimento
    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);

    private enum Phase { GROWING, HOLDING, SHRINKING }

    private Phase phase = Phase.GROWING;
    private int lifetime;
    private int holdTicksRemaining;
    private float maxRadius;
    private UUID ownerUUID;

    public BlackHoleEntity(EntityType<? extends BlackHoleEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BlackHoleEntity(Level world, double x, double y, double z, int lifetime, float maxRadius, UUID ownerUUID) {
        this(ModEntities.BLACK_HOLE.get(), world);
        this.setPos(x, y, z);
        this.lifetime = lifetime;
        this.maxRadius = maxRadius;
        this.ownerUUID = ownerUUID;
        this.entityData.set(DATA_RADIUS, MIN_RADIUS);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(DATA_RADIUS, MIN_RADIUS);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return; // fases só são calculadas no servidor; o cliente só lê DATA_RADIUS
        }

        if (this.lifetime-- <= 0 && this.phase != Phase.SHRINKING) {
            // tempo de vida a esgotar-se mas ainda não começou a encolher: força o encolhimento
            this.phase = Phase.SHRINKING;
        }

        float radius = this.entityData.get(DATA_RADIUS);

        switch (this.phase) {
            case GROWING -> {
                radius = Math.min(radius + GROWTH_RATE, this.maxRadius);
                if (radius >= this.maxRadius) {
                    this.phase = Phase.HOLDING;
                    this.holdTicksRemaining = DEFAULT_HOLD_TICKS;
                }
            }
            case HOLDING -> {
                if (this.holdTicksRemaining-- <= 0) {
                    this.phase = Phase.SHRINKING;
                }
            }
            case SHRINKING -> {
                radius = Math.max(radius - SHRINK_RATE, 0.0F);
                if (radius <= 0.0F) {
                    this.discard();
                    return;
                }
            }
        }

        this.entityData.set(DATA_RADIUS, radius);

        // Só suga blocos/entidades enquanto está a crescer ou parada no máximo, não enquanto encolhe
        if (this.phase != Phase.SHRINKING) {
            this.suckBlocksAndEntities();
        }
    }

    public float getCurrentRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void suckBlocksAndEntities() {
        float currentRadius = this.getCurrentRadius();
        int range = (int) Math.ceil(getCurrentRadius() * 3);
        BlockPos centerPos = this.blockPosition();

        List<BlockPos> blocksToRemove = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(centerPos.offset(-range, -range, -range), centerPos.offset(range, range, range))) {
            double distance = Math.sqrt(pos.distSqr(centerPos));
            if (distance <= currentRadius) {
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
            // Dropped items and experience are never pulled or destroyed (e.g. the Nether Star of a Wither)
            if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) continue;

            // Players holding the Wings of Doom in the MAIN hand (right) are not pulled or killed.
            // Checked every tick: changing slot (or holding it in the left hand) gives no protection
            if (entity instanceof Player player && player.getMainHandItem().is(ModItems.WINGS_OF_DOOM.get())) continue;

            // Big mobs made of parts (the Ender Dragon): the closest part counts
            Entity target = entity instanceof EnderDragonPart part ? part.parentMob : entity;
            Vec3 directionToBlackHole = this.position().subtract(entity.position()).normalize();
            double distance = entity.distanceTo(this);

            double innerZoneRadius = currentRadius * 1.5;
            double middleZoneRadius = currentRadius * 2.25;
            double outerZoneRadius = currentRadius * 3.0;

            double strength;

            if (distance <= currentRadius) {
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

            double killRadius = currentRadius * 0.6;//Area of killradius insade the Black Hole

            if (distance <= killRadius && this.level() instanceof ServerLevel level) {
                annihilate(level, target);
            }
        }
    }

    /// Kills anything inside the black hole, no matter what: creative players, bosses, mobs with
    /// immortality from other mods... Only the player who made the black hole and players holding the
    /// Wings of Doom in the main hand are safe (see suckBlocksAndEntities).
    private static void annihilate(ServerLevel level, Entity entity) {
        if (!entity.isAlive()) return;

        // The Ender Dragon only dies with its own death (it flies to the portal, explodes and drops its experience)
        if (entity instanceof EnderDragon dragon) {
            if (dragon.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
                dragon.setHealth(1.0F);
                dragon.getPhaseManager().setPhase(EnderDragonPhase.DYING);
            }
            return;
        }
        // Things that are not alive (arrows, boats, falling blocks...) just disappear
        if (!(entity instanceof LivingEntity living)) {
            entity.discard();
            return;
        }

        // Like the /kill command: also works on players in creative
        living.invulnerableTime = 0;
        living.kill(level);
        // Something stopped the death (e.g. armor that makes you immortal): force it
        if (living.isAlive()) {
            living.setHealth(0.0F);
            living.die(level.damageSources().genericKill());
        }
        // Still alive? Mobs are removed from the world (players can't be removed)
        if (living.isAlive() && !(living instanceof Player)) {
            living.discard();
        }
    }

    // The black hole is temporary: it is not saved with the world
    @Override
    protected void readAdditionalSaveData(ValueInput input) {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {}

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    // Nothing can hurt a black hole
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    public Level getLevel() {
        return this.level();
    }

    private boolean isLiquid(BlockState state) {
        return !state.getFluidState().isEmpty();
    }
}
