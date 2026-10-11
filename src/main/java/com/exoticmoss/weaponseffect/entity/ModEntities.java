package com.exoticmoss.weaponseffect.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.exoticmoss.weaponseffect.entity.custom.LanceProjectileEntity;

import static com.exoticmoss.weaponseffect.WeaponsEffect.MODID;


public class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BlackHoleEntity>> BLACK_HOLE =
            ENTITY_TYPES.registerEntityType("black_hole", BlackHoleEntity::new, MobCategory.MISC,
                    builder -> builder.sized(2.0F, 2.0F).noLootTable());

    public static final DeferredHolder<EntityType<?>, EntityType<LanceProjectileEntity>> LANCE =
            ENTITY_TYPES.registerEntityType("lance", LanceProjectileEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5F, 0.5F).noLootTable().clientTrackingRange(4).updateInterval(20));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
