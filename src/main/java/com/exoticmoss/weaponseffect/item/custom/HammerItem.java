package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.entity.custom.HammerShockwaveEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/// Hammer. Hits give Slowness. Ability "Explosion": right-click a block to make an explosion on top of it.
/// Every mob inside the explosion takes the SAME damage, close or far from the middle.
/// The explosion doesn't break blocks and never hurts the player using the hammer.
public class HammerItem extends Item {
    // ---------------------------------------------------------------- normal hits
    /// Slowness given when hitting a mob: seconds and level (0 = Slowness I, 3 = Slowness IV...).
    private static final int HIT_SLOWNESS_SECONDS = 10;
    private static final int HIT_SLOWNESS_AMPLIFIER = 3;

    // ---------------------------------------------------------------- ability
    /// Time between two explosions (ticks, 20 = 1 second).
    private static final int COOLDOWN_TICKS = 50;
    /// Size of the explosion (radius in blocks). The 3D shockwave has the same size.
    public static final float EXPLOSION_RADIUS = 7.5F;
    /// Damage done to every mob inside the explosion.
    private static final float EXPLOSION_DAMAGE = 40.0F;
    /// How strong the mobs are thrown away and up (0 = not moved at all).
    private static final double KNOCKBACK = 0.0;
    private static final double KNOCKBACK_UP = 0.0;
    /// Slowness given to the mobs inside the explosion: seconds and level (0 seconds = none).
    private static final int EXPLOSION_SLOWNESS_SECONDS = 0;
    private static final int EXPLOSION_SLOWNESS_AMPLIFIER = 3;

    public HammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, HIT_SLOWNESS_SECONDS * 20, HIT_SLOWNESS_AMPLIFIER), attacker);
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("hammer.tooltip").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("hammer.line.2"));
        tooltip.accept(Component.translatable("hammer.ability").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("hammer.ability.name").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("hammer.line.3"));
        tooltip.accept(Component.translatable("hammer.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    // Right-click on a block: the explosion on top of it
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) return InteractionResult.PASS;

        if (player.getCooldowns().isOnCooldown(stack)) {
            if (!context.getLevel().isClientSide()) player.sendOverlayMessage(Component.translatable("item.hammer.cooldown"));
            return InteractionResult.FAIL;
        }
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        if (context.getLevel() instanceof ServerLevel level) {
            BlockPos pos = context.getClickedPos();
            explode(level, player, Vec3.atBottomCenterOf(pos.above()));
        }
        return InteractionResult.SUCCESS;
    }

    private static void explode(ServerLevel level, Player player, Vec3 center) {
        // Every mob touching the sphere of the explosion gets the full damage
        AABB area = new AABB(center, center).inflate(EXPLOSION_RADIUS);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, area, mob -> mob != player)) {
            Vec3 closest = closestPoint(mob.getBoundingBox(), center);
            if (closest.distanceTo(center) > EXPLOSION_RADIUS) continue;

            mob.hurtServer(level, player.damageSources().playerAttack(player), EXPLOSION_DAMAGE);
            if (EXPLOSION_SLOWNESS_SECONDS > 0) {
                mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EXPLOSION_SLOWNESS_SECONDS * 20, EXPLOSION_SLOWNESS_AMPLIFIER), player);
            }
            if (KNOCKBACK > 0 || KNOCKBACK_UP > 0) {
                Vec3 away = mob.position().subtract(center).multiply(1, 0, 1).normalize().scale(KNOCKBACK);
                mob.push(away.x, KNOCKBACK_UP, away.z);
                mob.hurtMarked = true;
            }
        }

        // The 3D shockwave and the sound
        HammerShockwaveEntity shockwave = new HammerShockwaveEntity(ModEntities.HAMMER_SHOCKWAVE.get(), level);
        shockwave.setPos(center.x, center.y, center.z);
        level.addFreshEntity(shockwave);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.7F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 1.2F, 0.8F);
    }

    /// The point of the box nearest to the center (to know if a big mob touches the explosion).
    private static Vec3 closestPoint(AABB box, Vec3 point) {
        return new Vec3(Math.clamp(point.x, box.minX, box.maxX), Math.clamp(point.y, box.minY, box.maxY), Math.clamp(point.z, box.minZ, box.maxZ));
    }
}
