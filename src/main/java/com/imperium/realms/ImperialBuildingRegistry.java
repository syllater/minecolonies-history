package com.imperium.realms;

import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.NoPrivateCrafterWorkerModule;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ImperialBuildingRegistry {
    public static final DeferredRegister<BuildingEntry> BUILDINGS = DeferredRegister.create(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "buildings"), ImperiumRealms.MOD_ID);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> DIPLOMAT_WORK =
            new BuildingEntry.ModuleProducer<>("imperial_diplomat_work",
                    () -> new NoPrivateCrafterWorkerModule(ImperialJobRegistry.DIPLOMAT.get(),
                            Skill.Intelligence, Skill.Knowledge, true, building -> 1),
                    () -> WorkerBuildingModuleView::new);

    public static final DeferredHolder<BuildingEntry, BuildingEntry> IMPERIAL_CHANCERY =
            BUILDINGS.register("imperial_chancery", () -> new BuildingEntry.Builder()
                    .setBuildingBlock(ImperialBlockRegistry.IMPERIAL_CHANCERY.get())
                    .setBuildingProducer(ImperialChancery::new)
                    .setBuildingViewProducer(() -> ImperialChancery.View::new)
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "imperial_chancery"))
                    .addBuildingModuleProducer(DIPLOMAT_WORK)
                    .addBuildingModuleProducer(BuildingModules.MIN_STOCK)
                    .createBuildingEntry());

    private ImperialBuildingRegistry() { }
    public static void register(final IEventBus modEventBus) { BUILDINGS.register(modEventBus); }
}
