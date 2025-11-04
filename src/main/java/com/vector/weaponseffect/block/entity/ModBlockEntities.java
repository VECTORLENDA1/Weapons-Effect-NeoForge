package com.vector.weaponseffect.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.vector.weaponseffect.block.ModBlocks;
import com.vector.weaponseffect.block.entity.custom.SimpleCraftingTableEntity;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "weaponseffect");

public static final Supplier<BlockEntityType<SimpleCraftingTableEntity>> SIMPLE_CRAFTING_TABLE_BE =
        BLOCK_ENTITIES.register("simple_crafting_table_be", () -> BlockEntityType.Builder.of(
                SimpleCraftingTableEntity::new, ModBlocks.SIMPLE_CRAFTING_TABLE.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
