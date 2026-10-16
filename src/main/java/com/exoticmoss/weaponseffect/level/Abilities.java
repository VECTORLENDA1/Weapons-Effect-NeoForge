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

    /// When the ability can't be used right now (e.g. only on the ground). Checked on the server.
    public interface Condition {
        boolean canUse(Player player, int weaponLevel);
    }

    /// cooldowns = cooldown in ticks for each level { I, II, III, IV, V } (20 ticks = 1 second, 0 = none).
    public static InteractionResult use(Level level, Player player, InteractionHand hand, int[] cooldowns, String cooldownMessage, Ability ability) {
        return use(level, player, hand, cooldowns, cooldownMessage, (user, weaponLevel) -> true, null, ability);
    }

    /// Same, but the ability only works when "condition" is true; if not, "conditionMessage" is shown
    /// and no cooldown starts.
    public static InteractionResult use(Level level, Player player, InteractionHand hand, int[] cooldowns, String cooldownMessage,
                                        Condition condition, String conditionMessage, Ability ability) {
        ItemStack weapon = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(weapon)) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable(cooldownMessage));
            return InteractionResult.FAIL;
        }
        // Everything else happens on the server (it also sends the cooldown to the player's screen)
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

        int weaponLevel = WeaponLevels.level(weapon);
        if (!condition.canUse(player, weaponLevel)) {
            if (conditionMessage != null) player.sendOverlayMessage(Component.translatable(conditionMessage));
            return InteractionResult.FAIL;
        }
        int cooldown = WeaponLevels.pick(cooldowns, weaponLevel);
        if (cooldown > 0) player.getCooldowns().addCooldown(weapon, cooldown);
        ability.run(serverLevel, player, weapon, weaponLevel);
        return InteractionResult.SUCCESS;
    }
}
