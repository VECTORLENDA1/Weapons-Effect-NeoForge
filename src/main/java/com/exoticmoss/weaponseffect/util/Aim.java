package com.exoticmoss.weaponseffect.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/// Where a player is aiming: the first mob or block in the middle of the screen (up to "range" blocks).
public final class Aim {
    private Aim() {
    }

    public static Vec3 target(LivingEntity player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(range));

        // First block in the way
        HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) end = block.getLocation();

        // A mob before that block? Then aim at the middle of the mob
        EntityHitResult mob = ProjectileUtil.getEntityHitResult(player, eye, end,
                player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0),
                entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator(), eye.distanceToSqr(end));
        return mob != null ? mob.getEntity().getBoundingBox().getCenter() : end;
    }
}
