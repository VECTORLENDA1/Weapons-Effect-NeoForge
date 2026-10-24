package com.exoticmoss.weaponseffect.mixin.client;

import com.exoticmoss.weaponseffect.client.ClientFireImmunity;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Clear view inside lava while holding a fire proof weapon (see ClientFireImmunity).
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow
    private FogType getFogType(Camera camera) {
        throw new AssertionError();
    }

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void weaponseffect$lavaFog(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount,
                                       ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        ClientFireImmunity.onFog(this.getFogType(camera), cir.getReturnValue());
    }
}
