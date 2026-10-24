package com.exoticmoss.weaponseffect.client;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import com.exoticmoss.weaponseffect.client.renderer.BlackHoleRenderer;
import com.exoticmoss.weaponseffect.client.renderer.FlameWaveRenderer;
import com.exoticmoss.weaponseffect.client.renderer.ShockwaveRenderer;
import com.exoticmoss.weaponseffect.client.renderer.IceLotusRenderer;
import com.exoticmoss.weaponseffect.client.renderer.IceShardRenderer;
import com.exoticmoss.weaponseffect.client.renderer.PoisonCloudRenderer;
import com.exoticmoss.weaponseffect.client.renderer.DarknessSlamRenderer;
import com.exoticmoss.weaponseffect.client.renderer.DashRenderer;
import com.exoticmoss.weaponseffect.client.renderer.SmokeBombRenderer;
import com.exoticmoss.weaponseffect.client.renderer.SoulRenderer;
import com.exoticmoss.weaponseffect.client.renderer.ThrownAxeRenderer;
import com.exoticmoss.weaponseffect.client.renderer.SoulRitualRenderer;
import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.entity.client.LanceProjectileModel;
import com.exoticmoss.weaponseffect.entity.client.LanceProjectileRenderer;


// Client only: how the mod's entities are drawn
// (called from WeaponsEffectClient)
public class ClientModEvents {
    public static void registerLayers() {
        ModelLayerRegistry.registerModelLayer(LanceProjectileModel.LAYER_LOCATION, LanceProjectileModel::createBodyLayer);
    }

    public static void onRegisterEntityRenderers() {
        EntityRendererRegistry.register(ModEntities.BLACK_HOLE, BlackHoleRenderer::new);
        EntityRendererRegistry.register(ModEntities.LANCE, LanceProjectileRenderer::new);
        EntityRendererRegistry.register(ModEntities.WITHER_RAIN_SKULL, WitherSkullRenderer::new);
        EntityRendererRegistry.register(ModEntities.FLAME_WAVE, FlameWaveRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICE_SHARD, IceShardRenderer::new);
        EntityRendererRegistry.register(ModEntities.POISON_CLOUD, PoisonCloudRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHOCKWAVE, ShockwaveRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICE_LOTUS, IceLotusRenderer::new);
        EntityRendererRegistry.register(ModEntities.SOUL_RITUAL, SoulRitualRenderer::new);
        EntityRendererRegistry.register(ModEntities.SOUL, SoulRenderer::new);
        EntityRendererRegistry.register(ModEntities.SMOKE_BOMB, SmokeBombRenderer::new);
        EntityRendererRegistry.register(ModEntities.DASH, DashRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWN_AXE, ThrownAxeRenderer::new);
        EntityRendererRegistry.register(ModEntities.DARKNESS_SLAM, DarknessSlamRenderer::new);
    }
}
