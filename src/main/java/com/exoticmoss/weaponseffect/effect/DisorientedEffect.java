package com.exoticmoss.weaponseffect.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/// "Disoriented" (from the darkness zone of the Darkness Mace): the mob forgets its target and walks
/// around in zigzags, changing direction all the time. While it has this effect it can't hurt anyone
/// (see EffectEvents).
public class DisorientedEffect extends MobEffect {
    /// Every how many ticks the mob changes direction (20 ticks = 1 second).
    public static final int TURN_TICKS = 8;
    /// How far the mob walks each time it turns (blocks) and how fast.
    public static final double WANDER_DISTANCE = 4;
    public static final double WANDER_SPEED = 1.0;

    public DisorientedEffect() {
        super(MobEffectCategory.HARMFUL, 0x2A1240);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (!(entity instanceof Mob mob)) return true;
        mob.setTarget(null);
        if (mob.tickCount % TURN_TICKS == 0) {
            double angle = mob.getRandom().nextDouble() * Math.PI * 2;
            mob.getNavigation().moveTo(mob.getX() + Math.cos(angle) * WANDER_DISTANCE, mob.getY(),
                    mob.getZ() + Math.sin(angle) * WANDER_DISTANCE, WANDER_SPEED);
        }
        return true;
    }
}
