package com.vector.weaponseffect.block;


import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.vector.weaponseffect.block.custom.SimpleCraftingTable;
import com.vector.weaponseffect.item.ModItems;

import java.util.function.Supplier;

import static com.vector.weaponseffect.WeaponsEffect.MODID;


public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);



    //Entity Blocks
    public static final DeferredBlock<Block> SIMPLE_CRAFTING_TABLE = registerBlockItem("simple_crafting_table",
            () -> new SimpleCraftingTable(BlockBehaviour.Properties.of().strength(2.5f,10)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));




    public static <T extends Block> DeferredBlock<T> registerBlockItem(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    public static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void Register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
