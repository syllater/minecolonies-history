package com.imperium.realms.registry;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.block.BlockHutImperialArchive;
import com.imperium.realms.block.BlockHutImperialGuardTower;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Minecraft block and item registrations owned by Imperium. */
public final class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, ImperiumRealms.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ImperiumRealms.MOD_ID);

    public static final DeferredHolder<Block, BlockHutImperialArchive> IMPERIAL_ARCHIVE =
            BLOCKS.register("blockhutimperialarchive", BlockHutImperialArchive::new);

    public static final DeferredHolder<Item, BlockItem> IMPERIAL_ARCHIVE_ITEM =
            ITEMS.register("blockhutimperialarchive",
                    () -> new BlockItem(IMPERIAL_ARCHIVE.get(), new Item.Properties()));

    public static final DeferredHolder<Block, BlockHutImperialGuardTower> IMPERIAL_GUARD_TOWER =
            BLOCKS.register("blockhutimperialguardtower", BlockHutImperialGuardTower::new);

    public static final DeferredHolder<Item, BlockItem> IMPERIAL_GUARD_TOWER_ITEM =
            ITEMS.register("blockhutimperialguardtower",
                    () -> new BlockItem(IMPERIAL_GUARD_TOWER.get(), new Item.Properties()));

    private ModBlocks() {
    }

    public static void register(final IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
