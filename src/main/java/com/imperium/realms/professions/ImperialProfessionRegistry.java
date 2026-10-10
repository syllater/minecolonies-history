package com.imperium.realms.professions;

import com.imperium.realms.ImperiumRealms;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.items.ItemBlockHut;
import com.minecolonies.core.colony.buildings.DefaultBuildingInstance;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.core.colony.buildings.views.EmptyView;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Addon registries extending MineColonies' own synchronized job and building registries.
 *
 * <p>The work-hut schematic is an independent asset and is tracked separately;
 * registering the block and building type alone does not claim a completed
 * survival blueprint.</p>
 */
public final class ImperialProfessionRegistry {
    public static final ResourceLocation TAX_COLLECTOR_JOB_ID =
            ResourceLocation.fromNamespaceAndPath(ImperiumRealms.MOD_ID, "tax_collector");

    private static final ResourceKey<Registry<JobEntry>> MINECOLONIES_JOBS =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("minecolonies", "jobs"));
    private static final ResourceKey<Registry<BuildingEntry>> MINECOLONIES_BUILDINGS =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("minecolonies", "buildings"));

    private static final DeferredRegister<JobEntry> JOBS =
            DeferredRegister.create(MINECOLONIES_JOBS, ImperiumRealms.MOD_ID);
    private static final DeferredRegister<BuildingEntry> BUILDINGS =
            DeferredRegister.create(MINECOLONIES_BUILDINGS, ImperiumRealms.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, ImperiumRealms.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ImperiumRealms.MOD_ID);

    public static final DeferredHolder<JobEntry, JobEntry> TAX_COLLECTOR_JOB = JOBS.register(
            "tax_collector",
            () -> new JobEntry.Builder()
                    .setJobProducer(ImperialTaxCollectorJob::new)
                    .setJobViewProducer(() -> com.minecolonies.core.colony.jobs.views.DefaultJobView::new)
                    .setRegistryName(TAX_COLLECTOR_JOB_ID)
                    .createJobEntry());

    public static final DeferredHolder<Block, TaxCollectorHutBlock> TAX_COLLECTOR_HUT =
            BLOCKS.register("tax_collector_hut", TaxCollectorHutBlock::new);

    public static final DeferredHolder<Item, ItemBlockHut> TAX_COLLECTOR_HUT_ITEM = ITEMS.register(
            "tax_collector_hut",
            () -> new ItemBlockHut(TAX_COLLECTOR_HUT.get(), new Item.Properties()));

    private static final BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView>
            TAX_COLLECTOR_WORK = new BuildingEntry.ModuleProducer<>(
                    "imperium_tax_collector_work",
                    () -> new WorkerBuildingModule(
                            TAX_COLLECTOR_JOB.get(),
                            Skill.Knowledge,
                            Skill.Intelligence,
                            true,
                            building -> 1),
                    () -> WorkerBuildingModuleView::new);

    public static final DeferredHolder<BuildingEntry, BuildingEntry> TAX_COLLECTOR_BUILDING =
            BUILDINGS.register("tax_collector_hut", () -> new BuildingEntry.Builder()
                    .setBuildingBlock(TAX_COLLECTOR_HUT.get())
                    .setBuildingProducer((colony, position) ->
                            new DefaultBuildingInstance(colony, position, "imperial_tax_collector", 5))
                    .setBuildingViewProducer(() -> EmptyView::new)
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "tax_collector_hut"))
                    .addBuildingModuleProducer(TAX_COLLECTOR_WORK)
                    .createBuildingEntry());

    private ImperialProfessionRegistry() {
    }

    public static void register(final IEventBus modEventBus) {
        JOBS.register(modEventBus);
        BUILDINGS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
