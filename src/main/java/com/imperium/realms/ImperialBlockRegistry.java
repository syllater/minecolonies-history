package com.imperium.realms;

import com.minecolonies.api.items.ItemBlockHut;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ImperialBlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ImperiumRealms.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImperiumRealms.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImperiumRealms.MOD_ID);

    public static final DeferredBlock<ImperialChanceryBlock> IMPERIAL_CHANCERY =
            BLOCKS.register("imperial_chancery", ImperialChanceryBlock::new);
    public static final DeferredItem<ItemBlockHut> IMPERIAL_CHANCERY_ITEM =
            ITEMS.register("imperial_chancery",
                    () -> new ItemBlockHut(IMPERIAL_CHANCERY.get(), new Item.Properties()));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> IMPERIUM_TAB =
            CREATIVE_TABS.register("imperium", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.imperium_realms.imperium"))
                    .icon(() -> IMPERIAL_CHANCERY_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(IMPERIAL_CHANCERY_ITEM.get()))
                    .build());

    private ImperialBlockRegistry() { }
    public static void register(final IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }
}
