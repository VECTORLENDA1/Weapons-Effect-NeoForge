package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.entity.custom.ShockwaveEntity;
import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/// Hammer. Hits give Slowness. Ability "Explosion": right-click a block to make an explosion on top of it.
/// Every mob inside the explosion takes the SAME damage, close or far from the middle.
/// The explosion doesn't break blocks and never hurts the player using the hammer.
///  - Level III and up: the mobs are thrown up and away and stunned.
///  - Level V: a second, bigger explosion a moment after the first one.
///
/// TABLES: one number for each level { I, II, III, IV, V }.
public class HammerItem extends Item implements AbilityWeapon {
    // ---------------------------------------------------------------- normal hits
    /// Slowness given when hitting a mob: seconds and level (0 = Slowness I, 3 = Slowness IV...).
    private static final int HIT_SLOWNESS_SECONDS = 10;
    private static final int HIT_SLOWNESS_AMPLIFIER = 3;

    // ---------------------------------------------------------------- ability
    /// Time between two explosions (ticks, 20 = 1 second).
    public static final int[] COOLDOWN_TICKS = {50, 45, 40, 35, 0};
    /// Size of the explosion (radius in blocks) and the damage to every mob inside. The 3D shockwave has the same size.
    private static final float[] EXPLOSION_RADIUS = {7.5F, 8.5F, 9.5F, 10.5F, 12};
    private static final float[] EXPLOSION_DAMAGE = {40, 45, 50, 55, 65};
    /// How strong the mobs are thrown away and up (0 = not moved), and how long they are stunned (seconds).
    private static final double[] KNOCKBACK = {0, 0, 0.6, 0.7, 0.8};
    private static final double[] KNOCKBACK_UP = {0, 0, 0.8, 0.9, 1.0};
    private static final int[] STUN_SECONDS = {0, 0, 3, 3, 4};
    /// The second explosion (0 = none): radius, damage and how long after the first one (ticks).
    private static final float[] SECOND_EXPLOSION_RADIUS = {0, 0, 0, 0, 16};
    private static final float[] SECOND_EXPLOSION_DAMAGE = {0, 0, 0, 0, 40};
    private static final int SECOND_EXPLOSION_DELAY = 10;
    /// Color of the 3D shockwave (0xAARRGGBB).
    private static final int SHOCKWAVE_COLOR = 0x60E8F0FF;

    public HammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        WeaponLevels.rewardNormalHit(stack, attacker);
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
        int level = WeaponLevels.level(stack);
        int cooldown = WeaponLevels.pick(COOLDOWN_TICKS, level);
        if (cooldown > 0) player.getCooldowns().addCooldown(stack, cooldown);

        if (context.getLevel() instanceof ServerLevel server) {
            Vec3 center = Vec3.atBottomCenterOf(context.getClickedPos().above());
            ShockwaveEntity.blast(server, center, WeaponLevels.pick(EXPLOSION_RADIUS, level), SHOCKWAVE_COLOR, 0,
                    new ShockwaveEntity.Blast(player, this, WeaponLevels.pick(EXPLOSION_DAMAGE, level),
                            WeaponLevels.pick(KNOCKBACK, level), WeaponLevels.pick(KNOCKBACK_UP, level), WeaponLevels.pick(STUN_SECONDS, level)));

            float secondRadius = WeaponLevels.pick(SECOND_EXPLOSION_RADIUS, level);
            if (secondRadius > 0) {
                ShockwaveEntity.blast(server, center, secondRadius, SHOCKWAVE_COLOR, SECOND_EXPLOSION_DELAY,
                        new ShockwaveEntity.Blast(player, this, WeaponLevels.pick(SECOND_EXPLOSION_DAMAGE, level),
                                WeaponLevels.pick(KNOCKBACK, level), WeaponLevels.pick(KNOCKBACK_UP, level), 0));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
