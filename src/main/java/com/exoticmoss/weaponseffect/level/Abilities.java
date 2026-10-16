package com.exoticmoss.weaponseffect.level;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/// What every ability does when right-click is used: checks the cooldown, starts the cooldown
/// of the weapon's level and runs the ability with the weapon's level.
public final class Abilities {
    private Abilities() {
    }

    /// The ability itself (only runs on the server).
    public interface Ability {
        void run(ServerLevel level, Player player, ItemStack weapon, int weaponLevel);
    }

    /// cooldowns = cooldown in ticks for each level { I, II, III, IV, V } (20 ticks = 1 second, 0 = none).
    public static InteractionResult use(Level level, Player player, InteractionHand hand, int[] cooldowns, String cooldownMessage, Ability ability) {
        ItemStack weapon = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(weapon)) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable(cooldownMessage));
            return InteractionResult.FAIL;
        }
        int weaponLevel = WeaponLevels.level(weapon);
        int cooldown = WeaponLevels.pick(cooldowns, weaponLevel);
        if (cooldown > 0) player.getCooldowns().addCooldown(weapon, cooldown);
        if (level instanceof ServerLevel serverLevel) ability.run(serverLevel, player, weapon, weaponLevel);
        return InteractionResult.SUCCESS;
    }
}
