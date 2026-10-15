package com.exoticmoss.weaponseffect.ability;

import com.exoticmoss.weaponseffect.WeaponsEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/// The "wave of flames" of the Fire Sword ability: a curved wall of fire that comes out of the
/// player and travels forward, getting wider, burning and hurting every mob it passes through.
@EventBusSubscriber(modid = WeaponsEffect.MODID)
public class FlameWave {
    /// How far the wave goes (in blocks).
    public static final double LENGTH = 9.0;
    /// How many blocks the wave moves each tick (20 ticks = 1 second).
    public static final double SPEED = 0.6;
    /// Width of the wave when it comes out of the player and at the end (in blocks).
    public static final double START_WIDTH = 1.5;
    public static final double END_WIDTH = 6.0;
    /// Height of the wall of flames (in blocks).
    public static final double HEIGHT = 1.8;
    /// How much the wave is curved (the middle goes ahead of the sides), in blocks.
    public static final double CURVE = 0.8;
    /// Damage done to each mob the wave hits (only once per wave) and how long it burns (seconds).
    public static final float DAMAGE = 13.0F;
    public static final int FIRE_SECONDS = 5;

    private static final List<Wave> WAVES = new ArrayList<>();

    private static class Wave {
        final ServerLevel level;
        final LivingEntity owner;
        final Vec3 origin;
        final Vec3 forward;
        final Vec3 right;
        final Set<LivingEntity> hit = new HashSet<>();
        double distance = 0.5;

        Wave(ServerLevel level, LivingEntity owner) {
            this.level = level;
            this.owner = owner;
            this.origin = owner.position();
            Vec3 look = owner.getForward();
            this.forward = new Vec3(look.x, 0, look.z).normalize();
            this.right = new Vec3(-forward.z, 0, forward.x);
        }
    }

    /// Starts a wave of flames in front of the attacker.
    public static void launch(ServerLevel level, LivingEntity owner) {
        WAVES.add(new Wave(level, owner));
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.6F);
        level.playSound(null, owner.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 4.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        Iterator<Wave> iterator = WAVES.iterator();
        while (iterator.hasNext()) {
            Wave wave = iterator.next();
            if (wave.level != level) continue;
            tick(wave);
            wave.distance += SPEED;
            if (wave.distance > LENGTH || !wave.owner.isAlive()) iterator.remove();
        }
    }

    private static void tick(Wave wave) {
        ServerLevel level = wave.level;
        double progress = wave.distance / LENGTH;
        double width = Mth.lerp(progress, START_WIDTH, END_WIDTH);
        double half = width / 2;
        // The flames get a bit lower at the end, as the wave dies out
        double height = HEIGHT * (1.0 - 0.4 * progress);

        // ---- The wall of flames: columns along a curved line
        for (double x = -half; x <= half; x += 0.35) {
            double side = x / half; // -1 at the left edge, 1 at the right edge
            double ahead = wave.distance - CURVE * side * side;
            Vec3 base = wave.origin.add(wave.forward.scale(ahead)).add(wave.right.scale(x));
            double groundY = findGround(level, base);
            if (Double.isNaN(groundY)) continue; // inside a wall: no flames there

            for (double y = 0; y < height; y += 0.45) {
                double jitter = (level.getRandom().nextDouble() - 0.5) * 0.25;
                // count 0: the 3 numbers are the movement of the particle (forward and a bit up)
                level.sendParticles(ParticleTypes.FLAME, base.x + jitter, groundY + y, base.z + jitter, 0,
                        wave.forward.x * 0.15, 0.04, wave.forward.z * 0.15, 1.0);
            }
            // Embers and smoke on top
            if (level.getRandom().nextFloat() < 0.35f) {
                level.sendParticles(ParticleTypes.LAVA, base.x, groundY + 0.2, base.z, 1, 0, 0, 0, 0);
            }
            if (level.getRandom().nextFloat() < 0.25f) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, base.x, groundY + height, base.z, 0, 0, 0.06, 0, 1.0);
            }
        }

        // ---- Damage: everything inside the front of the wave
        Vec3 center = wave.origin.add(wave.forward.scale(wave.distance));
        AABB area = new AABB(center, center).inflate(half + 0.5, 0, half + 0.5).expandTowards(0, height + 0.5, 0).move(0, -0.5, 0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == wave.owner || target.fireImmune() || !wave.hit.add(target)) continue;
            // Only the mobs really close to the line of the wave (not the corners of the box)
            Vec3 offset = target.position().subtract(wave.origin);
            double along = offset.dot(wave.forward);
            if (Math.abs(along - wave.distance) > 1.5) {
                wave.hit.remove(target);
                continue;
            }
            target.setRemainingFireTicks(FIRE_SECONDS * 20);
            target.hurtServer(level, damageSource(wave.owner), DAMAGE);
        }
    }

    /// The height where the flames stand: the top of the ground at this place (looks 2 blocks up and down).
    /// NaN if the place is inside a wall.
    private static double findGround(ServerLevel level, Vec3 pos) {
        BlockPos start = BlockPos.containing(pos.x, pos.y + 0.5, pos.z);
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos up = start.above(dy);
            if (level.getBlockState(up).getCollisionShape(level, up).isEmpty()) {
                // free space: now go down until there is ground
                for (int down = 0; down <= 3; down++) {
                    BlockPos below = up.below(down + 1);
                    if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                        return below.getY() + 1.0;
                    }
                }
                return up.getY();
            }
        }
        return Double.NaN;
    }

    private static DamageSource damageSource(LivingEntity attacker) {
        return attacker instanceof Player player
                ? player.damageSources().playerAttack(player)
                : attacker.damageSources().mobAttack(attacker);
    }
}
