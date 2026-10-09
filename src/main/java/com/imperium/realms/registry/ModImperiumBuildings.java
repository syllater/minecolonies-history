package com.imperium.realms.registry;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.building.BuildingImperialArchive;
import com.imperium.realms.building.BuildingImperialGuardTower;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.core.colony.buildings.moduleviews.CombinedHiringLimitModuleView;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.GuardBuildingModule;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.views.EmptyView;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** MineColonies building registry integration for the Imperial Archive. */
public final class ModImperiumBuildings {
    private static final DeferredRegister<BuildingEntry> BUILDINGS =
            DeferredRegister.create(CommonMinecoloniesAPIImpl.BUILDINGS, ImperiumRealms.MOD_ID);

    public static final DeferredHolder<BuildingEntry, BuildingEntry> IMPERIAL_ARCHIVE =
            BUILDINGS.register("imperial_archive", () -> new BuildingEntry.Builder()
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "imperial_archive"))
                    .setBuildingBlock(ModBlocks.IMPERIAL_ARCHIVE.get())
                    .setBuildingProducer(BuildingImperialArchive::new)
                    .setBuildingViewProducer(() -> EmptyView::new)
                    .addBuildingModuleProducer(new BuildingEntry.ModuleProducer<>(
                            "imperium_philosopher_worker",
                            () -> new WorkerBuildingModule(
                                    ModImperiumJobs.PHILOSOPHER.get(),
                                    Skill.Knowledge,
                                    Skill.Stamina,
                                    true,
                                    building -> 1),
                            () -> WorkerBuildingModuleView::new))
                    .addBuildingModuleProducer(new BuildingEntry.ModuleProducer<>(
                            "imperium_tax_collector_worker",
                            () -> new WorkerBuildingModule(
                                    ModImperiumJobs.TAX_COLLECTOR.get(),
                                    Skill.Knowledge,
                                    Skill.Stamina,
                                    true,
                                    building -> 1),
                            () -> WorkerBuildingModuleView::new))
                    .addBuildingModuleProducer(new BuildingEntry.ModuleProducer<>(
                            "imperium_diplomat_worker",
                            () -> new WorkerBuildingModule(
                                    ModImperiumJobs.DIPLOMAT.get(),
                                    Skill.Knowledge,
                                    Skill.Stamina,
                                    true,
                                    building -> 1),
                            () -> WorkerBuildingModuleView::new))
                    .createBuildingEntry());

    public static final DeferredHolder<BuildingEntry, BuildingEntry> IMPERIAL_GUARD_TOWER =
            BUILDINGS.register("imperial_guard_tower", () -> new BuildingEntry.Builder()
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "imperial_guard_tower"))
                    .setBuildingBlock(ModBlocks.IMPERIAL_GUARD_TOWER.get())
                    .setBuildingProducer(BuildingImperialGuardTower::new)
                    .setBuildingViewProducer(() -> BuildingImperialGuardTower.View::new)
                    .addBuildingModuleProducer(new BuildingEntry.ModuleProducer<>(
                            "imperium_siege_engineer_work",
                            () -> new GuardBuildingModule(
                                    ModImperiumGuardTypes.SIEGE_ENGINEER.get(),
                                    true,
                                    building -> building.getBuildingLevel()),
                            () -> CombinedHiringLimitModuleView::new))
                    .addBuildingModuleProducer(new BuildingEntry.ModuleProducer<>(
                            "imperium_field_medic_work",
                            () -> new GuardBuildingModule(
                                    ModImperiumGuardTypes.FIELD_MEDIC.get(),
                                    true,
                                    building -> building.getBuildingLevel()),
                            () -> CombinedHiringLimitModuleView::new))
                    .addBuildingModuleProducer(new BuildingEntry.ModuleProducer<>(
                            "imperium_cavalier_work",
                            () -> new GuardBuildingModule(
                                    ModImperiumGuardTypes.CAVALIER.get(),
                                    true,
                                    building -> building.getBuildingLevel()),
                            () -> CombinedHiringLimitModuleView::new))
                    .addBuildingModuleProducer(BuildingModules.GUARD_TOOL)
                    .addBuildingModuleProducer(BuildingModules.GUARD_ENTITY_LIST)
                    .addBuildingModuleProducer(BuildingModules.GUARD_SETTINGS)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .addBuildingModuleProducer(BuildingModules.BED)
                    .addBuildingModuleProducer(BuildingModules.STATS_MODULE)
                    .createBuildingEntry());

    private ModImperiumBuildings() {
    }

    public static void register(final IEventBus modEventBus) {
        BUILDINGS.register(modEventBus);
    }
}
