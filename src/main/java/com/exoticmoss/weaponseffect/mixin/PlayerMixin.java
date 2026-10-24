package com.exoticmoss.weaponseffect.mixin;

import com.exoticmoss.weaponseffect.entity.custom.DashEntity;
import com.exoticmoss.weaponseffect.event.EventEffectItems;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Fabric has no player tick event: the start and the end of Player.tick() (both sides) call the mod here.
@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void weaponseffect$tickPre(CallbackInfo ci) {
        EventEffectItems.onPlayerTickPre((Player) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void weaponseffect$tickPost(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        EventEffectItems.onPlayerTickPost(player);
        DashEntity.Landing.onPlayerTick(player);
    }
}
