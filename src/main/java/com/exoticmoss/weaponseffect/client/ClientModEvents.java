package com.exoticmoss.weaponseffect.client;

import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.exoticmoss.weaponseffect.WeaponsEffect;
import com.exoticmoss.weaponseffect.client.renderer.BlackHoleRenderer;
import com.exoticmoss.weaponseffect.client.renderer.FlameWaveRenderer;
import com.exoticmoss.weaponseffect.client.renderer.ShockwaveRenderer;
import com.exoticmoss.weaponseffect.client.renderer.IceLotusRenderer;
import com.exoticmoss.weaponseffect.client.renderer.IceShardRenderer;
import com.exoticmoss.weaponseffect.client.renderer.PoisonCloudRenderer;
import com.exoticmoss.weaponseffect.client.renderer.SoulRenderer;
import com.exoticmoss.weaponseffect.client.renderer.SoulRitualRenderer;
import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.entity.client.LanceProjectileModel;
import com.exoticmoss.weaponseffect.entity.client.LanceProjectileRenderer;


// Client only: how the mod's entities are drawn
@EventBusSubscriber(modid = WeaponsEffect.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LanceProjectileModel.LAYER_LOCATION, LanceProjectileModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLACK_HOLE.get(), BlackHoleRenderer::new);
        event.registerEntityRenderer(ModEntities.LANCE.get(), LanceProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.WITHER_RAIN_SKULL.get(), WitherSkullRenderer::new);
        event.registerEntityRenderer(ModEntities.FLAME_WAVE.get(), FlameWaveRenderer::new);
        event.registerEntityRenderer(ModEntities.ICE_SHARD.get(), IceShardRenderer::new);
        event.registerEntityRenderer(ModEntities.POISON_CLOUD.get(), PoisonCloudRenderer::new);
        event.registerEntityRenderer(ModEntities.SHOCKWAVE.get(), ShockwaveRenderer::new);
        event.registerEntityRenderer(ModEntities.ICE_LOTUS.get(), IceLotusRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_RITUAL.get(), SoulRitualRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL.get(), SoulRenderer::new);
    }
}
