package com.imperium.realms.colony;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.registry.ModBlocks;
import com.imperium.realms.registry.ModImperiumBuildings;
import com.imperium.realms.registry.ModImperiumGuardTypes;
import com.imperium.realms.registry.ModImperiumJobs;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.ldtteam.structurize.storage.StructurePacks;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Validates the packaged Structurize pack from the actual server-side loader.
 * This is intentionally read-only; it never edits player worlds or blocks.
 *
 * <p>The check runs once per server after Structurize has registered packs, and
 * logs one searchable result per build. It is useful for dedicated-server CI
 * and catches missing resources or malformed NBT before a player opens a
 * Builder's Hut.</p>
 */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class BlueprintPackValidator {
    public static final String PACK_NAME = "imperium_european";
    public static final String PASS_MARKER = "IMPERIUM_BLUEPRINT_VALIDATION_PASS";
    public static final String REGISTRY_PASS_MARKER = "IMPERIUM_REGISTRY_VALIDATION_PASS";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WAIT_LIMIT_TICKS = 1_200;

    private record BlueprintSpec(
            String path,
            int sizeX,
            int sizeY,
            int sizeZ,
            Supplier<? extends Block> anchor) {
    }

    private static final List<BlueprintSpec> SPECS = List.of(
            new BlueprintSpec("buildings/imperial_archive/imperialarchive1.blueprint", 7, 5, 7,
                    () -> ModBlocks.IMPERIAL_ARCHIVE.get()),
            new BlueprintSpec("buildings/imperial_archive/imperialarchive2.blueprint", 7, 6, 7,
                    () -> ModBlocks.IMPERIAL_ARCHIVE.get()),
            new BlueprintSpec("buildings/imperial_archive/imperialarchive3.blueprint", 9, 7, 9,
                    () -> ModBlocks.IMPERIAL_ARCHIVE.get()),
            new BlueprintSpec("buildings/imperial_archive/imperialarchive4.blueprint", 9, 8, 9,
                    () -> ModBlocks.IMPERIAL_ARCHIVE.get()),
            new BlueprintSpec("buildings/imperial_archive/imperialarchive5.blueprint", 11, 9, 11,
                    () -> ModBlocks.IMPERIAL_ARCHIVE.get()),
            new BlueprintSpec("buildings/imperial_guard_tower/imperialguardtower1.blueprint", 7, 5, 7,
                    () -> ModBlocks.IMPERIAL_GUARD_TOWER.get()),
            new BlueprintSpec("buildings/imperial_guard_tower/imperialguardtower2.blueprint", 7, 7, 7,
                    () -> ModBlocks.IMPERIAL_GUARD_TOWER.get()),
            new BlueprintSpec("buildings/imperial_guard_tower/imperialguardtower3.blueprint", 9, 8, 9,
                    () -> ModBlocks.IMPERIAL_GUARD_TOWER.get()),
            new BlueprintSpec("buildings/imperial_guard_tower/imperialguardtower4.blueprint", 9, 9, 9,
                    () -> ModBlocks.IMPERIAL_GUARD_TOWER.get()),
            new BlueprintSpec("buildings/imperial_guard_tower/imperialguardtower5.blueprint", 11, 11, 11,
                    () -> ModBlocks.IMPERIAL_GUARD_TOWER.get()));

    private static MinecraftServer trackedServer;
    private static int ticksWaited;
    private static boolean completed;

    private BlueprintPackValidator() {
    }

    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Post event) {
        final MinecraftServer server = event.getServer();
        if (server != trackedServer) {
            trackedServer = server;
            ticksWaited = 0;
            completed = false;
        }
        if (completed) {
            return;
        }

        ticksWaited++;
        if (ticksWaited % 20 != 0) {
            return;
        }

        if (!StructurePacks.hasPack(PACK_NAME)) {
            if (ticksWaited >= WAIT_LIMIT_TICKS) {
                LOGGER.error("{}: Structurize did not register pack '{}' within {} server ticks.",
                        "IMPERIUM_BLUEPRINT_VALIDATION_FAIL", PACK_NAME, WAIT_LIMIT_TICKS);
                completed = true;
            }
            return;
        }

        if (!validateMineColoniesRegistrations()) {
            completed = true;
            return;
        }
        validate(server.overworld());
        completed = true;
    }

    /**
     * Validates the actual MineColonies registry bindings after all mod registries
     * have completed loading. This catches a build where blueprints exist but a
     * job, hut entry, worker module or guard-job binding was accidentally omitted.
     */
    private static boolean validateMineColoniesRegistrations() {
        try {
            final List<JobEntry> jobs = List.of(
                    ModImperiumJobs.PHILOSOPHER.get(),
                    ModImperiumJobs.TAX_COLLECTOR.get(),
                    ModImperiumJobs.DIPLOMAT.get(),
                    ModImperiumJobs.IMPERIAL_SIEGE_ENGINEER.get(),
                    ModImperiumJobs.IMPERIAL_FIELD_MEDIC.get(),
                    ModImperiumJobs.IMPERIAL_CAVALIER.get());
            final List<String> expectedJobPaths = List.of(
                    "philosopher",
                    "tax_collector",
                    "diplomat",
                    "imperial_siege_engineer",
                    "imperial_field_medic",
                    "imperial_cavalier");

            for (int index = 0; index < jobs.size(); index++) {
                final ResourceLocation expected = ResourceLocation.fromNamespaceAndPath(
                        ImperiumRealms.MOD_ID, expectedJobPaths.get(index));
                if (!expected.equals(jobs.get(index).getKey())) {
                    LOGGER.error("IMPERIUM_REGISTRY_VALIDATION_FAIL: job entry '{}' resolved to '{}'.",
                            expected, jobs.get(index).getKey());
                    return false;
                }
            }

            final BuildingEntry archive = ModImperiumBuildings.IMPERIAL_ARCHIVE.get();
            final BuildingEntry guardTower = ModImperiumBuildings.IMPERIAL_GUARD_TOWER.get();
            if (!ResourceLocation.fromNamespaceAndPath(ImperiumRealms.MOD_ID, "imperial_archive")
                    .equals(archive.getRegistryName())
                    || archive.getBuildingBlock() != ModBlocks.IMPERIAL_ARCHIVE.get()) {
                LOGGER.error("IMPERIUM_REGISTRY_VALIDATION_FAIL: Imperial Archive building entry/block binding is invalid.");
                return false;
            }
            if (!ResourceLocation.fromNamespaceAndPath(ImperiumRealms.MOD_ID, "imperial_guard_tower")
                    .equals(guardTower.getRegistryName())
                    || guardTower.getBuildingBlock() != ModBlocks.IMPERIAL_GUARD_TOWER.get()) {
                LOGGER.error("IMPERIUM_REGISTRY_VALIDATION_FAIL: Imperial Guard Tower building entry/block binding is invalid.");
                return false;
            }

            final Set<String> archiveModules = moduleKeys(archive);
            final Set<String> guardTowerModules = moduleKeys(guardTower);
            final Set<String> expectedArchiveModules = Set.of(
                    "imperium_philosopher_worker",
                    "imperium_tax_collector_worker",
                    "imperium_diplomat_worker");
            final Set<String> expectedGuardModules = Set.of(
                    "imperium_siege_engineer_work",
                    "imperium_field_medic_work",
                    "imperium_cavalier_work");
            if (!archiveModules.containsAll(expectedArchiveModules)) {
                LOGGER.error("IMPERIUM_REGISTRY_VALIDATION_FAIL: Archive worker modules missing; found {}.",
                        archiveModules);
                return false;
            }
            if (!guardTowerModules.containsAll(expectedGuardModules)) {
                LOGGER.error("IMPERIUM_REGISTRY_VALIDATION_FAIL: Guard Tower specialist modules missing; found {}.",
                        guardTowerModules);
                return false;
            }

            if (!guardUsesJob(ModImperiumGuardTypes.SIEGE_ENGINEER.get(), "imperial_siege_engineer")
                    || !guardUsesJob(ModImperiumGuardTypes.FIELD_MEDIC.get(), "imperial_field_medic")
                    || !guardUsesJob(ModImperiumGuardTypes.CAVALIER.get(), "imperial_cavalier")) {
                LOGGER.error("IMPERIUM_REGISTRY_VALIDATION_FAIL: a guard type is not bound to its expected MineColonies job.");
                return false;
            }

            LOGGER.info("{}: jobs=6 buildings=2 guardTypes=3 modules=6.",
                    REGISTRY_PASS_MARKER);
            return true;
        } catch (final RuntimeException exception) {
            LOGGER.error("IMPERIUM_REGISTRY_VALIDATION_FAIL: exception while checking MineColonies registry bindings.",
                    exception);
            return false;
        }
    }

    private static Set<String> moduleKeys(final BuildingEntry entry) {
        return entry.getModuleProducers().stream()
                .map(module -> module.key)
                .collect(Collectors.toSet());
    }

    private static boolean guardUsesJob(final GuardType guard, final String jobPath) {
        final JobEntry job = guard.getJobEntry().get();
        return job != null && ResourceLocation.fromNamespaceAndPath(
                ImperiumRealms.MOD_ID, jobPath).equals(job.getKey());
    }

    private static void validate(final ServerLevel level) {
        int passed = 0;
        for (final BlueprintSpec spec : SPECS) {
            try {
                final Blueprint blueprint = StructurePacks.getBlueprint(
                        PACK_NAME, spec.path(), level.registryAccess());
                if (blueprint == null) {
                    LOGGER.error("IMPERIUM_BLUEPRINT_VALIDATION_FAIL: missing/unreadable blueprint '{}'.",
                            spec.path());
                    continue;
                }

                final BlockPos offset = blueprint.getPrimaryBlockOffset();
                final Block expectedAnchor = spec.anchor().get();
                final boolean correctAnchorOffset =
                        offset.getX() == spec.sizeX() / 2 && offset.getY() == 0 && offset.getZ() == 0;
                final boolean containsAnchor = Arrays.stream(blueprint.getPalette())
                        .anyMatch(blockState -> blockState.getBlock() == expectedAnchor);

                if (blueprint.getSizeX() != spec.sizeX()
                        || blueprint.getSizeY() != spec.sizeY()
                        || blueprint.getSizeZ() != spec.sizeZ()
                        || !correctAnchorOffset
                        || !containsAnchor) {
                    LOGGER.error(
                            "IMPERIUM_BLUEPRINT_VALIDATION_FAIL: '{}' has size {}x{}x{}, anchorOffset={}, expected {}x{}x{}, offset=(center,0,0), anchor={}.",
                            spec.path(),
                            blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ(),
                            offset,
                            spec.sizeX(), spec.sizeY(), spec.sizeZ(), expectedAnchor);
                    continue;
                }

                passed++;
                LOGGER.info("IMPERIUM_BLUEPRINT_VALIDATION_ITEM_PASS: '{}' ({}x{}x{}).",
                        spec.path(), spec.sizeX(), spec.sizeY(), spec.sizeZ());
            } catch (final Exception exception) {
                LOGGER.error("IMPERIUM_BLUEPRINT_VALIDATION_FAIL: exception while loading '{}'.",
                        spec.path(), exception);
            }
        }

        if (passed == SPECS.size()) {
            LOGGER.info("{}: {}/{} Structurize blueprints loaded with valid dimensions and hut anchors from pack '{}'.",
                    PASS_MARKER, passed, SPECS.size(), PACK_NAME);
        } else {
            LOGGER.error("IMPERIUM_BLUEPRINT_VALIDATION_FAIL: {}/{} Structurize blueprints passed from pack '{}'.",
                    passed, SPECS.size(), PACK_NAME);
        }
    }
}
