package com.exoticmoss.weaponseffect.level;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/// Command to test the levels (needs cheats / operator), always on the weapon in the main hand:
///  - /weaponlevel set <1-100> -> puts the weapon at that level (with 0 XP) and shows what changed
///                               (above 5 only with this command: see WeaponLevels.COMMAND_MAX_LEVEL)
///  - /weaponlevel xp <amount> -> gives XP to the weapon
///  - /weaponlevel ready       -> fills the XP, so the weapon is ready for the Soul Ritual
public class WeaponLevelCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> dispatcher.register(Commands.literal("weaponlevel")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("set")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, WeaponLevels.COMMAND_MAX_LEVEL))
                                .executes(c -> setLevel(c, IntegerArgumentType.getInteger(c, "level")))))
                .then(Commands.literal("xp")
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                .executes(c -> addXp(c, FloatArgumentType.getFloat(c, "amount")))))
                .then(Commands.literal("ready").executes(WeaponLevelCommand::ready))));
    }

    private static int setLevel(CommandContext<CommandSourceStack> context, int level) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack weapon = weapon(context, player);
        if (weapon == null) return 0;
        int from = WeaponLevels.level(weapon);
        WeaponLevels.set(weapon, level, 0);
        if (level > from) {
            LevelUpChanges.tell(player, weapon, from, level);
        } else {
            context.getSource().sendSuccess(() -> Component.translatable("weaponseffect.command.level", weapon.getHoverName(), level), false);
        }
        return 1;
    }

    private static int addXp(CommandContext<CommandSourceStack> context, float amount) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack weapon = weapon(context, player);
        if (weapon == null) return 0;
        int level = WeaponLevels.level(weapon);
        float xp = Math.min(WeaponLevels.xpNeeded(level), WeaponLevels.get(weapon).xp() + amount);
        WeaponLevels.set(weapon, level, xp);
        context.getSource().sendSuccess(() -> Component.translatable("weaponseffect.command.xp", weapon.getHoverName(),
                WeaponLevels.formatXp(xp), WeaponLevels.xpNeeded(level)), false);
        return 1;
    }

    private static int ready(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack weapon = weapon(context, player);
        if (weapon == null) return 0;
        int level = WeaponLevels.level(weapon);
        if (level >= WeaponLevels.MAX_LEVEL) {
            context.getSource().sendFailure(Component.translatable("weaponseffect.level.max"));
            return 0;
        }
        WeaponLevels.set(weapon, level, WeaponLevels.xpNeeded(level));
        context.getSource().sendSuccess(() -> Component.translatable("weaponseffect.level.ready", weapon.getHoverName(), level + 1), false);
        return 1;
    }

    /// The weapon with levels in the main hand (null and a message if there is none).
    private static ItemStack weapon(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof AbilityWeapon) return stack;
        context.getSource().sendFailure(Component.translatable("weaponseffect.command.no_weapon"));
        return null;
    }
}
