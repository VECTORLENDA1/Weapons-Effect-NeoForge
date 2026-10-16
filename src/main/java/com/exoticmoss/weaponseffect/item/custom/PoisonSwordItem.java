package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.level.Abilities;
import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import com.exoticmoss.weaponseffect.entity.custom.PoisonCloudEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

/// Poison Sword. Hits poison the mob.
/// Ability "Poison Cloud": right-click to make a poison cloud where you aim
/// (size, duration and poison in PoisonCloudEntity).
public class PoisonSwordItem extends Item implements AbilityWeapon {
    /// Time between two uses of the ability for each level { I, II, III, IV, V } (ticks, 20 = 1 second).
    public static final int[] COOLDOWN_TICKS = {200, 180, 160, 140, 0};
    /// Poison of a hit: seconds and level.
    private static final int HIT_POISON_SECONDS = 10;
    private static final int HIT_POISON_AMPLIFIER = 100;

    public PoisonSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        WeaponLevels.rewardNormalHit(stack, attacker);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, HIT_POISON_SECONDS * 20, HIT_POISON_AMPLIFIER), attacker);
        super.hurtEnemy(stack, target, attacker);
    }

    // Right-click: the ability
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return Abilities.use(level, player, hand, COOLDOWN_TICKS, "item.poison.sword.cooldown",
                (server, user, weapon, weaponLevel) -> PoisonCloudEntity.launch(server, user, weaponLevel));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("poison.sword.tooltip").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("poison.line.2"));
        tooltip.accept(Component.translatable("poison.immunities").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("poison.immunities.tooltip").withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.translatable("poison.line.3"));
        tooltip.accept(Component.translatable("poison.ability").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.translatable("poison.ability.name").withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.translatable("poison.line.4"));
        tooltip.accept(Component.translatable("poison.ability.description").withStyle(ChatFormatting.WHITE));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
