package com.imperium.realms.compat;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

/**
 * Extends MineColonies' shared hut block-entity type with Imperium's own hut
 * blocks using NeoForge's supported extension event.
 *
 * <p>MineColonies constructs its shared {@code minecolonies:colonybuilding}
 * type from the built-in hut list. Merely extending {@code AbstractBlockHut}
 * does not add an addon block to that type's valid-block set; Minecraft would
 * otherwise reject the new block entity when the hut is placed in-world.</p>
 */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class MineColoniesBlockEntityCompatibility {
    private static final ResourceKey<BlockEntityType<?>> MINECOLONIES_BUILDING_TYPE =
            ResourceKey.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath("minecolonies", "colonybuilding"));

    private MineColoniesBlockEntityCompatibility() {
    }

    @SubscribeEvent
    public static void addImperiumHuts(final BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                MINECOLONIES_BUILDING_TYPE,
                ModBlocks.IMPERIAL_ARCHIVE.get(),
                ModBlocks.IMPERIAL_GUARD_TOWER.get());
    }
}
