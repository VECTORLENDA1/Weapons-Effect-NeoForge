package com.exoticmoss.weaponseffect.client;

import com.exoticmoss.weaponseffect.event.EventEffectItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;

/// What the player sees while holding the Fire Sword or the Wings of Doom:
/// no fire on the screen, and the lava doesn't cover the view (you can see inside it).
/// (Called from the client mixins ScreenEffectRendererMixin and FogRendererMixin.)
public class ClientFireImmunity {
    /// How far (in blocks) the player can see inside lava while holding one of these weapons.
    public static final float LAVA_VIEW_DISTANCE = 48.0F;

    /// No fire on the screen (first person)
    public static boolean hidesFireOverlay(Player player) {
        return EventEffectItems.isFireProof(player);
    }

    /// Clear view inside lava
    public static void onFog(FogType type, FogData fog) {
        if (type != FogType.LAVA) return;
        Player player = Minecraft.getInstance().player;
        if (player == null || !EventEffectItems.isFireProof(player)) return;

        fog.environmentalStart = 0.0F;
        fog.environmentalEnd = Math.max(fog.environmentalEnd, LAVA_VIEW_DISTANCE);
    }
}
