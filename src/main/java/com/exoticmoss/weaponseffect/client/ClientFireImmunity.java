package com.exoticmoss.weaponseffect.client;

import com.exoticmoss.weaponseffect.WeaponsEffect;
import com.exoticmoss.weaponseffect.event.EventEffectItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/// What the player sees while holding the Fire Sword or the Wings of Doom:
/// no fire on the screen, and the lava doesn't cover the view (you can see inside it).
@EventBusSubscriber(modid = WeaponsEffect.MODID, value = Dist.CLIENT)
public class ClientFireImmunity {
    /// How far (in blocks) the player can see inside lava while holding one of these weapons.
    public static final float LAVA_VIEW_DISTANCE = 48.0F;

    /// No fire on the screen (first person)
    @SubscribeEvent
    public static void onScreenEffect(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE && EventEffectItems.isFireProof(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    /// Clear view inside lava
    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        if (event.getType() != FogType.LAVA) return;
        Player player = Minecraft.getInstance().player;
        if (player == null || !EventEffectItems.isFireProof(player)) return;

        event.setNearPlaneDistance(0.0F);
        event.setFarPlaneDistance(Math.max(event.getFarPlaneDistance(), LAVA_VIEW_DISTANCE));
    }
}
