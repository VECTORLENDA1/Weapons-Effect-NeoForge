package com.exoticmoss.weaponseffect.client;

import com.exoticmoss.weaponseffect.WeaponsEffect;
import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/// Shows the level and the ability experience of the weapons in their tooltip.
@EventBusSubscriber(modid = WeaponsEffect.MODID, value = Dist.CLIENT)
public class ClientWeaponLevelTooltip {
    private static final int[] ROMAN_VALUES = {100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] ROMAN_LETTERS = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    /// 1 -> "I", 5 -> "V", 42 -> "XLII", 100 -> "C".
    private static String roman(int number) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (number >= ROMAN_VALUES[i]) {
                text.append(ROMAN_LETTERS[i]);
                number -= ROMAN_VALUES[i];
            }
        }
        return text.toString();
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AbilityWeapon)) return;

        int level = WeaponLevels.level(stack);
        var lines = event.getToolTip();
        int at = Math.min(1, lines.size()); // right under the name
        lines.add(at, Component.translatable("weaponseffect.level", roman(level)).withStyle(ChatFormatting.LIGHT_PURPLE));

        Component progress;
        if (level >= WeaponLevels.MAX_LEVEL) {
            progress = Component.translatable("weaponseffect.level.max").withStyle(ChatFormatting.GOLD);
        } else if (WeaponLevels.isReady(stack)) {
            progress = Component.translatable("weaponseffect.level.ready_tooltip", level).withStyle(ChatFormatting.AQUA);
        } else {
            progress = Component.translatable("weaponseffect.level.xp", WeaponLevels.formatXp(WeaponLevels.get(stack).xp()), WeaponLevels.xpNeeded(level))
                    .withStyle(ChatFormatting.DARK_PURPLE);
        }
        lines.add(at + 1, progress);
    }
}
