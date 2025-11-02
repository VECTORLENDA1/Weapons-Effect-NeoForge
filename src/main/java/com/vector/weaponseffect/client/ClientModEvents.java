package com.vector.weaponseffect.client;

import net.minecraft.client.particle.GustParticle;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import com.vector.weaponseffect.WeaponsEffect;
import com.vector.weaponseffect.client.renderer.BlackHoleRenderer;
import com.vector.weaponseffect.entity.ModEntities;
import com.vector.weaponseffect.registry.ModParticles;


@EventBusSubscriber(modid = WeaponsEffect.MODID, bus  = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntities.BLACK_HOLE.get(), BlackHoleRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.GUST.get(), GustParticle.Provider::new);
    }


}
