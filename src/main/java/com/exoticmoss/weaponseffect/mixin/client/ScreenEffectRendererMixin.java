package com.exoticmoss.weaponseffect.mixin.client;

import com.exoticmoss.weaponseffect.client.ClientFireImmunity;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/// No fire on the screen (first person) while holding a fire proof weapon (see ClientFireImmunity).
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @Redirect(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
    private boolean weaponseffect$hideFire(net.minecraft.client.player.LocalPlayer player) {
        return player.isOnFire() && !ClientFireImmunity.hidesFireOverlay(player);
    }
}
