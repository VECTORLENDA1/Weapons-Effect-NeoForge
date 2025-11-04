package com.vector.weaponseffect;

import com.mojang.logging.LogUtils;
import com.vector.weaponseffect.recipe.ModRecipes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import com.vector.weaponseffect.block.ModBlocks;
import com.vector.weaponseffect.block.entity.ModBlockEntities;
import com.vector.weaponseffect.client.renderer.BlackHoleRenderer;
import com.vector.weaponseffect.effect.ModEffect;
import com.vector.weaponseffect.entity.ModEntities;
import com.vector.weaponseffect.entity.client.LanceProjectileRenderer;
import com.vector.weaponseffect.item.ModCreativeModTabs;
import com.vector.weaponseffect.item.ModItems;
import com.vector.weaponseffect.registry.ModParticles;
import com.vector.weaponseffect.screen.ModMenuTypes;
import com.vector.weaponseffect.screen.custom.SimpleCraftingTableScreen;
import org.slf4j.Logger;

@Mod(WeaponsEffect.MODID)
public class WeaponsEffect {
    public static final String MODID = "weaponseffect";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WeaponsEffect(IEventBus modEventBus, ModContainer modContainer) {


        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        ModItems.Register(modEventBus);
        ModBlocks.Register(modEventBus);
        ModEffect.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModEntities.register(modEventBus);
        ModParticles.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.BLACK_HOLE.get(), BlackHoleRenderer::new);
            EntityRenderers.register(ModEntities.LANCE.get(), LanceProjectileRenderer::new);

        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.SIMPLE_CRAFTING_TABLE_MENU.get(), SimpleCraftingTableScreen::new);
        }

    }
}
