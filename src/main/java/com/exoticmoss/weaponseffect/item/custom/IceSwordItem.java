package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.entity.custom.IceShardEntity;
import com.exoticmoss.weaponseffect.level.Abilities;
import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import com.exoticmoss.weaponseffect.util.Freeze;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/// Ice Sword. Hits freeze the mob (the freezing of powder snow: frost, shaking, frost damage) and make it very slow.
/// Ability "Ice Shard": right-click to make ice shards appear above you; they fly to where you aim
/// (amount, damage and the effects of each level in IceShardEntity).
public class IceSwordItem extends Item implements AbilityWeapon {
    /// Time between two uses of the ability for each level { I, II, III, IV, V } (ticks, 20 = 1 second).
    private static final int[] COOLDOWN_TICKS = {60, 50, 40, 30, 0};
    /// Freezing of a hit: how long it lasts (seconds) and the level of the Slowness.
    private static final int HIT_FREEZE_SECONDS = 5;
    private static final int HIT_SLOWNESS_AMPLIFIER = 100;

    public IceSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        WeaponLevels.rewardNormalHit(stack, attacker);
        if (!target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, HIT_FREEZE_SECONDS * 20, HIT_SLOWNESS_AMPLIFIER), attacker);
            Freeze.apply(target, HIT_FREEZE_SECONDS);
        }
        super.hurtEnemy(stack, target, attacker);
    }

    // Right-click: the ability
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return Abilities.use(level, player, hand, COOLDOWN_TICKS, "item.ice.sword.cooldown",
                (server, user, weapon, weaponLevel) -> IceShardEntity.launch(server, user, weaponLevel));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("ice.sword.tooltip").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("ice.line.2"));
        tooltip.accept(Component.translatable("ice.immunities").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("ice.immunities.tooltip").withStyle(ChatFormatting.BLUE));
        tooltip.accept(Component.translatable("ice.line.3"));
        tooltip.accept(Component.translatable("ice.ability").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("ice.ability.name").withStyle(ChatFormatting.BLUE));
        tooltip.accept(Component.translatable("ice.line.4"));
        tooltip.accept(Component.translatable("ice.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
