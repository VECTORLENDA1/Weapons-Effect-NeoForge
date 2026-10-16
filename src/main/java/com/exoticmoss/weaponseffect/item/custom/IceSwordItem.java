package com.exoticmoss.weaponseffect.item.custom;

import com.exoticmoss.weaponseffect.entity.custom.IceShardEntity;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.WeakHashMap;
import java.util.function.Consumer;

/// Ice Sword. Hits freeze the mob: heavy Slowness and a bit of damage every second for a while.
/// Ability "Ice Shard": right-click to make ice shards appear above you; they fly to where you aim
/// (amount, damage and speed in IceShardEntity).
public class IceSwordItem extends Item {
    /// Time between two uses of the ability (ticks, 20 = 1 second).
    private static final int COOLDOWN_TICKS = 60;
    /// Freezing of a hit: how long it lasts (ticks) and the damage done every second.
    private static final int FREEZE_TICKS = 100;
    private static final float FREEZE_DAMAGE = 1.0F;

    /// The mobs being frozen -> ticks left.
    private static final WeakHashMap<LivingEntity, Integer> FROZEN = new WeakHashMap<>();

    public IceSwordItem(Properties properties) {
        super(properties);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, FREEZE_TICKS, 100), attacker);
            FROZEN.put(target, FREEZE_TICKS);
        }
        super.hurtEnemy(stack, target, attacker);
    }

    // Every second: the frozen mobs take a bit of damage and stay slow
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        FROZEN.entrySet().removeIf(entry -> {
            LivingEntity mob = entry.getKey();
            int ticksLeft = entry.getValue();
            if (mob == null || !mob.isAlive() || ticksLeft <= 0) return true;
            if (ticksLeft % 20 == 0 && mob.level() instanceof ServerLevel level) {
                mob.hurtServer(level, mob.damageSources().freeze(), FREEZE_DAMAGE);
                mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, FREEZE_TICKS, 100));
            }
            entry.setValue(ticksLeft - 1);
            return false;
        });
    }

    // Right-click: the ability
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("item.ice.sword.cooldown"));
            return InteractionResult.FAIL;
        }
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        if (level instanceof ServerLevel serverLevel) IceShardEntity.launch(serverLevel, player);
        return InteractionResult.SUCCESS;
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
