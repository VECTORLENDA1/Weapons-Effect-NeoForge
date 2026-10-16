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
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V"};

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AbilityWeapon)) return;

        int level = WeaponLevels.level(stack);
        var lines = event.getToolTip();
        int at = Math.min(1, lines.size()); // right under the name
        lines.add(at, Component.translatable("weaponseffect.level", ROMAN[level - 1]).withStyle(ChatFormatting.LIGHT_PURPLE));

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
