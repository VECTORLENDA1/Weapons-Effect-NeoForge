package com.exoticmoss.weaponseffect.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.exoticmoss.weaponseffect.entity.custom.LanceProjectileEntity;
import com.exoticmoss.weaponseffect.entity.custom.WitherRainSkull;
import com.exoticmoss.weaponseffect.entity.custom.FlameWaveEntity;
import com.exoticmoss.weaponseffect.entity.custom.ShockwaveEntity;
import com.exoticmoss.weaponseffect.entity.custom.IceLotusEntity;
import com.exoticmoss.weaponseffect.entity.custom.IceShardEntity;
import com.exoticmoss.weaponseffect.entity.custom.PoisonCloudEntity;
import com.exoticmoss.weaponseffect.entity.custom.DarknessSlamEntity;
import com.exoticmoss.weaponseffect.entity.custom.DashEntity;
import com.exoticmoss.weaponseffect.entity.custom.SmokeBombEntity;
import com.exoticmoss.weaponseffect.entity.custom.ThrownAxeEntity;
import com.exoticmoss.weaponseffect.ritual.SoulEntity;
import com.exoticmoss.weaponseffect.ritual.SoulRitualEntity;

import static com.exoticmoss.weaponseffect.WeaponsEffect.MODID;


public class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BlackHoleEntity>> BLACK_HOLE =
            ENTITY_TYPES.registerEntityType("black_hole", BlackHoleEntity::new, MobCategory.MISC,
                    builder -> builder.sized(2.0F, 2.0F).noLootTable());

    public static final DeferredHolder<EntityType<?>, EntityType<LanceProjectileEntity>> LANCE =
            ENTITY_TYPES.registerEntityType("lance", LanceProjectileEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(4).updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<WitherRainSkull>> WITHER_RAIN_SKULL =
            ENTITY_TYPES.registerEntityType("wither_rain_skull", WitherRainSkull::new, MobCategory.MISC,
                    builder -> builder.sized(0.3125F, 0.3125F).noLootTable().clientTrackingRange(4).updateInterval(10));

    public static final DeferredHolder<EntityType<?>, EntityType<FlameWaveEntity>> FLAME_WAVE =
            ENTITY_TYPES.registerEntityType("flame_wave", FlameWaveEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(8).updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<IceShardEntity>> ICE_SHARD =
            ENTITY_TYPES.registerEntityType("ice_shard", IceShardEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.3F, 0.3F).noLootTable().clientTrackingRange(8).updateInterval(1));

    public static final DeferredHolder<EntityType<?>, EntityType<PoisonCloudEntity>> POISON_CLOUD =
            ENTITY_TYPES.registerEntityType("poison_cloud", PoisonCloudEntity::new, MobCategory.MISC,
                    builder -> builder.sized(1.0F, 1.0F).noLootTable().clientTrackingRange(8).updateInterval(2));

    public static final DeferredHolder<EntityType<?>, EntityType<ShockwaveEntity>> SHOCKWAVE =
            ENTITY_TYPES.registerEntityType("shockwave", ShockwaveEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(10).updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<IceLotusEntity>> ICE_LOTUS =
            ENTITY_TYPES.registerEntityType("ice_lotus", IceLotusEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.8F, 0.4F).noLootTable().clientTrackingRange(8).updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<SoulRitualEntity>> SOUL_RITUAL =
            ENTITY_TYPES.registerEntityType("soul_ritual", SoulRitualEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(10).updateInterval(2));

    public static final DeferredHolder<EntityType<?>, EntityType<SoulEntity>> SOUL =
            ENTITY_TYPES.registerEntityType("soul", SoulEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.3F, 0.3F).noLootTable().clientTrackingRange(8).updateInterval(1));

    public static final DeferredHolder<EntityType<?>, EntityType<SmokeBombEntity>> SMOKE_BOMB =
            ENTITY_TYPES.registerEntityType("smoke_bomb", SmokeBombEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(8).updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<DashEntity>> DASH =
            ENTITY_TYPES.registerEntityType("dash", DashEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(8).updateInterval(1));

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownAxeEntity>> THROWN_AXE =
            ENTITY_TYPES.registerEntityType("thrown_axe", ThrownAxeEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.8F, 0.8F).noLootTable().clientTrackingRange(8).updateInterval(1));

    public static final DeferredHolder<EntityType<?>, EntityType<DarknessSlamEntity>> DARKNESS_SLAM =
            ENTITY_TYPES.registerEntityType("darkness_slam", DarknessSlamEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(8).updateInterval(1));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
