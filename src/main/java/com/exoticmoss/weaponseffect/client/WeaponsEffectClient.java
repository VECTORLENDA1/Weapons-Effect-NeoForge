package com.exoticmoss.weaponseffect.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

/// Client entrypoint: renderers, model layers and tooltips
/// (the fire overlay and the lava fog are changed by the client mixins).
public class WeaponsEffectClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientModEvents.registerLayers();
        ClientModEvents.onRegisterEntityRenderers();
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> ClientWeaponLevelTooltip.onTooltip(stack, lines));
    }
}
