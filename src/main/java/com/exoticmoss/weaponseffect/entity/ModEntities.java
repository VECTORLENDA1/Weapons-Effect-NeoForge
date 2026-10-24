package com.exoticmoss.weaponseffect.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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

import java.util.function.UnaryOperator;

import static com.exoticmoss.weaponseffect.WeaponsEffect.MODID;


public class ModEntities {
    /// Registers an entity type (the builder gets the id, as vanilla requires).
    private static <E extends Entity> EntityType<E> registerEntityType(String name, EntityType.EntityFactory<E> factory, MobCategory category,
                                                                      UnaryOperator<EntityType.Builder<E>> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.apply(EntityType.Builder.of(factory, category)).build(key));
    }

    public static final EntityType<BlackHoleEntity> BLACK_HOLE =
            registerEntityType("black_hole", BlackHoleEntity::new, MobCategory.MISC,
                    builder -> builder.sized(2.0F, 2.0F).noLootTable());

    public static final EntityType<LanceProjectileEntity> LANCE =
            registerEntityType("lance", LanceProjectileEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(4).updateInterval(20));

    public static final EntityType<WitherRainSkull> WITHER_RAIN_SKULL =
            registerEntityType("wither_rain_skull", WitherRainSkull::new, MobCategory.MISC,
                    builder -> builder.sized(0.3125F, 0.3125F).noLootTable().clientTrackingRange(4).updateInterval(1));

    public static final EntityType<FlameWaveEntity> FLAME_WAVE =
            registerEntityType("flame_wave", FlameWaveEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(8).updateInterval(20));

    public static final EntityType<IceShardEntity> ICE_SHARD =
            registerEntityType("ice_shard", IceShardEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.3F, 0.3F).noLootTable().clientTrackingRange(8).updateInterval(1));

    public static final EntityType<PoisonCloudEntity> POISON_CLOUD =
            registerEntityType("poison_cloud", PoisonCloudEntity::new, MobCategory.MISC,
                    builder -> builder.sized(1.0F, 1.0F).noLootTable().clientTrackingRange(8).updateInterval(2));

    public static final EntityType<ShockwaveEntity> SHOCKWAVE =
            registerEntityType("shockwave", ShockwaveEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(10).updateInterval(20));

    public static final EntityType<IceLotusEntity> ICE_LOTUS =
            registerEntityType("ice_lotus", IceLotusEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.8F, 0.4F).noLootTable().clientTrackingRange(8).updateInterval(20));

    public static final EntityType<SoulRitualEntity> SOUL_RITUAL =
            registerEntityType("soul_ritual", SoulRitualEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(10).updateInterval(2));

    public static final EntityType<SoulEntity> SOUL =
            registerEntityType("soul", SoulEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.3F, 0.3F).noLootTable().clientTrackingRange(8).updateInterval(1));

    public static final EntityType<SmokeBombEntity> SMOKE_BOMB =
            registerEntityType("smoke_bomb", SmokeBombEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(8).updateInterval(20));

    public static final EntityType<DashEntity> DASH =
            registerEntityType("dash", DashEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(8).updateInterval(1));

    public static final EntityType<ThrownAxeEntity> THROWN_AXE =
            registerEntityType("thrown_axe", ThrownAxeEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.8F, 0.8F).noLootTable().clientTrackingRange(8).updateInterval(1));

    public static final EntityType<DarknessSlamEntity> DARKNESS_SLAM =
            registerEntityType("darkness_slam", DarknessSlamEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(8).updateInterval(1));

    /// Loads the class, so all the entity types above are registered.
    public static void register() {
    }
}
