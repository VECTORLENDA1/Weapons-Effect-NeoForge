package com.vector.weaponseffect.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.vector.weaponseffect.entity.custom.LanceProjectileEntity;

import java.util.function.Supplier;

import static com.vector.weaponseffect.WeaponsEffect.MODID;


public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);

    public static final Supplier<EntityType<BlackHoleEntity>> BLACK_HOLE =
            ENTITY_TYPES.register("black_hole", () -> EntityType.Builder.<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
                    .sized(2.0F, 2.0F)
                    .build("black_hole"));

    public static final Supplier<EntityType<LanceProjectileEntity>> LANCE =
            ENTITY_TYPES.register("lance", () -> EntityType.Builder.<LanceProjectileEntity>of(LanceProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("lance"));



    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
