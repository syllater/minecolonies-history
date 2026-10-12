package com.imperium.realms.colony;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.building.BuildingImperialArchive;
import com.imperium.realms.building.BuildingImperialGuardTower;
import com.imperium.realms.registry.ModBlocks;
import com.imperium.realms.registry.ModImperiumBuildings;
import com.imperium.realms.registry.ModImperiumJobs;
import com.imperium.realms.registry.ModImperiumGuardTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.modules.GuardBuildingModule;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Live Minecraft runtime tests for persistence paths that pure unit tests
 * cannot fully exercise. These run under runGameTestServer in CI.
 */
@GameTestHolder(ImperiumRealms.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EmpireRuntimeGameTests {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final GameProfile COLONY_TEST_OWNER = new GameProfile(
            UUID.fromString("54dfb4b8-9cc3-4a52-8c7b-2a1b9f18c911"),
            "imperium_gametest");

    private EmpireRuntimeGameTests() {
    }

    @GameTest(template = "flat", timeoutTicks = 100)
    public static void savedEmpireEconomyAndRealmRoundTrip(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final long gameTime = Math.max(0L, level.getGameTime());
        final long dayIndex = Math.max(0L,
                Math.floorDiv(level.getServer().overworld().getGameTime(), 24_000L));
        final String dimension = level.dimension().location().toString();
        final String runId = UUID.randomUUID().toString();
        final int capitalId = uniqueTestColonyId();
        final int provinceId = capitalId == Integer.MAX_VALUE ? capitalId - 1 : capitalId + 1;
        final ColonyIdentity capital = new ColonyIdentity(dimension, capitalId);
        final ColonyIdentity province = new ColonyIdentity("minecraft:the_nether", provinceId);
        final EmpireStateSavedData live = EmpireStateSavedData.get(level);

        require(live.observeColony(capital, "GameTest Capital " + runId, gameTime),
                "Expected a new test capital state");
        require(live.observeColony(province, "GameTest Province " + runId, gameTime),
                "Expected a new test province state");

        final EmpireState capitalState = live.get(capital).orElseThrow();
        final EmpireState provinceState = live.get(province).orElseThrow();
        require(capitalState.creditTreasury(250L), "Could not seed capital treasury");
        require(provinceState.creditTreasury(500L), "Could not seed province treasury");
        require(provinceState.collectDailyTaxes(dayIndex, 10L) == 10L,
                "First daily tax turn should collect the expected crowns");
        require(provinceState.collectDailyTaxes(dayIndex, 10L) == 0L,
                "The same daily tax turn must not collect twice");
        require(provinceState.treasuryCrowns() == 510L,
                "Repeated daily tax processing changed the provincial balance");

        require(capitalState.recordScholarWork(gameTime, 5, "philosopher-" + runId),
                "Tier-five Philosopher work should succeed");
        require(capitalState.recordTaxCollectorWork(gameTime, 5, "tax-collector-" + runId),
                "Tier-five Tax Collector work should succeed");
        require(capitalState.recordDiplomatWork(gameTime, 5, "diplomat-" + runId),
                "Tier-five Diplomat work should succeed");

        final EmpireRealm realm = live.createRealm(
                "GameTest Realm " + runId.substring(0, 8), capital, "uuid-" + runId,
                "GameTest Emperor", dayIndex).orElseThrow(
                () -> new AssertionError("Could not create a test realm"));
        require(realm.setImperialTaxRatePercent(12), "Could not set test tax law");
        require(realm.setImperialEconomicPolicy(EconomicPolicy.WELFARE),
                "Could not set test economic law");
        require(live.inviteProvince(realm.id(), province, dayIndex),
                "Could not invite test province");
        require(live.acceptRealmInvitation(province, dayIndex).isPresent(),
                "Could not accept test invitation");

        require(live.depositIntoImperialTreasury(province, 125L, "GameTest Emperor", dayIndex)
                        == EmpireStateSavedData.TreasuryTransferResult.TRANSFERRED,
                "Could not transfer province funds to the imperial treasury");

        final CompoundTag serialized = live.save(new CompoundTag(), level.registryAccess());
        final EmpireStateSavedData restored =
                EmpireStateSavedData.load(serialized, level.registryAccess());
        final EmpireState restoredCapital = restored.get(capital).orElseThrow(
                () -> new AssertionError("Capital state was missing after NBT load"));
        final EmpireState restoredProvince = restored.get(province).orElseThrow(
                () -> new AssertionError("Province state was missing after NBT load"));
        final EmpireRealm restoredRealm = restored.realmById(realm.id()).orElseThrow(
                () -> new AssertionError("Realm was missing after NBT load"));

        require(restoredCapital.treasuryCrowns() == 250L, "Capital treasury changed after NBT load");
        require(restoredCapital.knowledgePoints() == 3L, "Knowledge progress changed after NBT load");
        require(restoredCapital.taxCollectionEfficiencyPercent() == 3,
                "Tax Collector progress changed after NBT load");
        require(restoredCapital.diplomaticInfluence() == 3L,
                "Diplomatic influence changed after NBT load");
        require(!restoredCapital.recordScholarWork(gameTime, 5, "philosopher-" + runId),
                "Saved worker cooldown allowed duplicate knowledge");
        require(!restoredCapital.recordTaxCollectorWork(gameTime, 5, "tax-collector-" + runId),
                "Saved worker cooldown allowed duplicate tax training");
        require(!restoredCapital.recordDiplomatWork(gameTime, 5, "diplomat-" + runId),
                "Saved worker cooldown allowed duplicate diplomatic influence");

        require(restoredProvince.treasuryCrowns() == 385L,
                "Tax receipt and treasury transfer did not survive NBT load");
        require(restoredProvince.lastTaxDay() == dayIndex,
                "Saved daily tax marker did not survive NBT load");
        require(restoredProvince.collectDailyTaxes(dayIndex, 10L) == 0L,
                "Reloaded state allowed the same daily tax turn to run twice");
        require(restoredProvince.taxRatePercent() == 12,
                "Realm tax law was not inherited by the province");
        require(restoredProvince.economicPolicy() == EconomicPolicy.WELFARE,
                "Realm economic law was not inherited by the province");
        require(restoredRealm.imperialTreasuryCrowns() == 125L,
                "Imperial treasury did not retain the committed transfer");
        require(restoredRealm.containsProvince(capital) && restoredRealm.containsProvince(province),
                "Realm membership did not survive NBT load");
        require(restoredRealm.recentAuditEntries().size() == 1
                        && "deposit".equals(restoredRealm.recentAuditEntries().get(0).actionId()),
                "Successful treasury transfer audit did not survive NBT load");

        LOGGER.info("IMPERIUM_GAMETEST_PASS: live SavedData daily tax idempotency, economy, worker cooldown, realm and treasury round-trip");
        helper.succeed();
    }

    @GameTest(template = "flat", timeoutTicks = 100)
    public static void discoversRealMineColoniesColonyAndPersistsState(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final ServerPlayer owner = FakePlayerFactory.get(level, COLONY_TEST_OWNER);
        final BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        final IColony colony = IColonyManager.getInstance().createColony(
                level, center, owner, "Imperium Integration Test", "Colonial");
        require(colony != null, "MineColonies did not create the real integration-test colony");

        try {
            final ColonyIdentity identity = ColonyIdentity.from(colony);
            final IColony located = MineColoniesIntegration.colonyAt(level, center).orElse(null);
            require(located != null, "MineColonies position lookup did not resolve the created colony");
            require(identity.equals(ColonyIdentity.from(located)),
                    "MineColonies position lookup returned a different colony");

            // Exercise the same discovery path used by the server lifecycle event.
            MineColoniesIntegration.synchronizeLoadedColonies(level);
            final EmpireStateSavedData data = EmpireStateSavedData.get(level);
            final EmpireState state = data.get(identity).orElseThrow(
                    () -> new AssertionError("Imperium did not discover the real MineColonies colony"));
            final long firstSeen = state.firstSeenGameTime();

            require(state.creditTreasury(7L), "Could not mutate the test colony's Imperium state");
            colony.setName("Imperium Renamed Colony");
            MineColoniesIntegration.synchronizeLoadedColonies(level);

            final EmpireState renamed = data.get(identity).orElseThrow(
                    () -> new AssertionError("Colony state disappeared after a second discovery pass"));
            require("Imperium Renamed Colony".equals(renamed.colonyName()),
                    "Imperium did not refresh the changed MineColonies colony name");
            require(renamed.firstSeenGameTime() == firstSeen,
                    "Repeated discovery reset the original first-seen timestamp");
            require(renamed.treasuryCrowns() == 7L,
                    "Repeated discovery reset the existing Imperium treasury");

            final CompoundTag snapshot = data.save(new CompoundTag(), level.registryAccess());
            final EmpireStateSavedData restored =
                    EmpireStateSavedData.load(snapshot, level.registryAccess());
            final EmpireState restoredState = restored.get(identity).orElseThrow(
                    () -> new AssertionError("Real colony's Imperium record did not survive NBT round-trip"));
            require(restoredState.treasuryCrowns() == 7L,
                    "Real colony's Imperium treasury changed after NBT round-trip");
            require(restoredState.firstSeenGameTime() == firstSeen,
                    "Real colony's first-seen timestamp changed after NBT round-trip");
            require("Imperium Renamed Colony".equals(restoredState.colonyName()),
                    "Real colony's latest name did not survive NBT round-trip");

            LOGGER.info("IMPERIUM_GAMETEST_PASS: real MineColonies colony discovery, rename preservation and Imperium SavedData round-trip");
        } finally {
            // This is a disposable GameTest colony; never leave it registered for later tests.
            IColonyManager.getInstance().deleteColonyByWorld(colony.getID(), false, level);
        }

        helper.succeed();
    }

    @GameTest(template = "flat", timeoutTicks = 100)
    public static void imperialArchiveRegistersWorkersAndScalesHiringLimit(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final ServerPlayer owner = FakePlayerFactory.get(level, COLONY_TEST_OWNER);
        final BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        final IColony colony = IColonyManager.getInstance().createColony(
                level, center, owner, "Imperium Archive Test", "Colonial");
        require(colony != null, "MineColonies did not create the Archive test colony");

        try {
            final Block hutBlock = ModBlocks.IMPERIAL_ARCHIVE.get();
            final BlockPos hutPos = helper.absolutePos(new BlockPos(2, 1, 2));
            final BlockState hutState = hutBlock.defaultBlockState();
            level.setBlock(hutPos, hutState, 3);
            hutBlock.setPlacedBy(level, hutPos, hutState, owner, ItemStack.EMPTY);

            IBuilding registered = IColonyManager.getInstance().getBuilding(level, hutPos);
            if (registered == null && level.getBlockEntity(hutPos) instanceof AbstractTileEntityColonyBuilding tile) {
                // Keep the same fallback that MineColonies addon GameTests use when placement
                // hasn't yet populated the manager index for this test tick.
                registered = colony.getServerBuildingManager().addNewBuilding(tile, level);
            }
            require(registered instanceof BuildingImperialArchive,
                    "Placed Archive hut block did not register a BuildingImperialArchive");

            final BuildingImperialArchive archive = (BuildingImperialArchive) registered;
            require(archive.getBuildingType().equals(ModImperiumBuildings.IMPERIAL_ARCHIVE.get()),
                    "Placed Archive building is linked to the wrong MineColonies BuildingEntry");
            require(archive.getMaxBuildingLevel() == 5,
                    "Imperial Archive must support five building tiers");

            final var workerModules = archive.getModules(WorkerBuildingModule.class);
            require(workerModules.size() == 3,
                    "Imperial Archive must expose Philosopher, Tax Collector and Diplomat worker modules");

            final WorkerBuildingModule philosopher = findWorkerModule(workerModules, ModImperiumJobs.PHILOSOPHER.get());
            final WorkerBuildingModule taxCollector = findWorkerModule(workerModules, ModImperiumJobs.TAX_COLLECTOR.get());
            final WorkerBuildingModule diplomat = findWorkerModule(workerModules, ModImperiumJobs.DIPLOMAT.get());
            require(philosopher != null && taxCollector != null && diplomat != null,
                    "One or more custom Archive worker modules are missing");

            archive.setBuildingLevel(1);
            require(philosopher.getModuleMax() == 1
                            && taxCollector.getModuleMax() == 1
                            && diplomat.getModuleMax() == 1,
                    "Level 1 Archive must limit each profession to one worker");

            final ICitizenData philosopherData = colony.getCitizenManager().createAndRegisterCivilianData();
            require(philosopher.assignCitizen(philosopherData),
                    "Level 1 Archive refused its first Philosopher");
            require(philosopherData.getJob() != null
                            && philosopherData.getJob().getJobRegistryEntry().equals(ModImperiumJobs.PHILOSOPHER.get()),
                    "MineColonies did not assign the registered Philosopher job");
            final ICitizenData extraPhilosopher = colony.getCitizenManager().createAndRegisterCivilianData();
            require(!philosopher.assignCitizen(extraPhilosopher),
                    "Level 1 Archive accepted a second Philosopher beyond its hiring limit");

            final ICitizenData taxCollectorData = colony.getCitizenManager().createAndRegisterCivilianData();
            require(taxCollector.assignCitizen(taxCollectorData),
                    "Level 1 Archive refused its first Tax Collector");
            require(taxCollectorData.getJob() != null
                            && taxCollectorData.getJob().getJobRegistryEntry().equals(ModImperiumJobs.TAX_COLLECTOR.get()),
                    "MineColonies did not assign the registered Tax Collector job");

            final ICitizenData diplomatData = colony.getCitizenManager().createAndRegisterCivilianData();
            require(diplomat.assignCitizen(diplomatData),
                    "Level 1 Archive refused its first Diplomat");
            require(diplomatData.getJob() != null
                            && diplomatData.getJob().getJobRegistryEntry().equals(ModImperiumJobs.DIPLOMAT.get()),
                    "MineColonies did not assign the registered Diplomat job");

            archive.setBuildingLevel(5);
            require(philosopher.getModuleMax() == 5
                            && taxCollector.getModuleMax() == 5
                            && diplomat.getModuleMax() == 5,
                    "Level 5 Archive must allow five workers per profession");

            for (int index = 0; index < 4; index++) {
                final ICitizenData additionalPhilosopher = colony.getCitizenManager().createAndRegisterCivilianData();
                require(philosopher.assignCitizen(additionalPhilosopher),
                        "Level 5 Archive refused a Philosopher below the five-worker cap");
            }
            require(philosopher.getAssignedCitizen().size() == 5,
                    "Level 5 Philosopher module did not reach its five-worker capacity");
            require(!philosopher.assignCitizen(colony.getCitizenManager().createAndRegisterCivilianData()),
                    "Level 5 Archive accepted a sixth Philosopher beyond its hiring limit");

            LOGGER.info("IMPERIUM_GAMETEST_PASS: real Imperial Archive hut registration, MineColonies jobs and level-scaled worker capacity");
        } finally {
            IColonyManager.getInstance().deleteColonyByWorld(colony.getID(), false, level);
        }

        helper.succeed();
    }

    @GameTest(template = "flat", timeoutTicks = 100)
    public static void imperialGuardTowerRegistersSpecialistsAndScalesGuardCapacity(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final ServerPlayer owner = FakePlayerFactory.get(level, COLONY_TEST_OWNER);
        final BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        final IColony colony = IColonyManager.getInstance().createColony(
                level, center, owner, "Imperium Guard Tower Test", "Colonial");
        require(colony != null, "MineColonies did not create the Guard Tower test colony");

        try {
            final Block hutBlock = ModBlocks.IMPERIAL_GUARD_TOWER.get();
            final BlockPos hutPos = helper.absolutePos(new BlockPos(2, 1, 2));
            final BlockState hutState = hutBlock.defaultBlockState();
            level.setBlock(hutPos, hutState, 3);
            hutBlock.setPlacedBy(level, hutPos, hutState, owner, ItemStack.EMPTY);

            IBuilding registered = IColonyManager.getInstance().getBuilding(level, hutPos);
            if (registered == null
                    && level.getBlockEntity(hutPos) instanceof AbstractTileEntityColonyBuilding tile) {
                registered = colony.getServerBuildingManager().addNewBuilding(tile, level);
            }
            require(registered instanceof BuildingImperialGuardTower,
                    "Placed Guard Tower hut block did not register a BuildingImperialGuardTower");

            final BuildingImperialGuardTower tower = (BuildingImperialGuardTower) registered;
            require(tower.getBuildingType().equals(ModImperiumBuildings.IMPERIAL_GUARD_TOWER.get()),
                    "Placed Guard Tower is linked to the wrong MineColonies BuildingEntry");
            require(tower.getMaxBuildingLevel() == 5,
                    "Imperial Guard Tower must support five building tiers");

            final var guardModules = tower.getModules(GuardBuildingModule.class);
            require(guardModules.size() == 3,
                    "Imperial Guard Tower must expose Siege Engineer, Field Medic and Cavalier modules");

            final GuardBuildingModule siegeEngineer =
                    findGuardModule(guardModules, ModImperiumJobs.IMPERIAL_SIEGE_ENGINEER.get());
            final GuardBuildingModule fieldMedic =
                    findGuardModule(guardModules, ModImperiumJobs.IMPERIAL_FIELD_MEDIC.get());
            final GuardBuildingModule cavalier =
                    findGuardModule(guardModules, ModImperiumJobs.IMPERIAL_CAVALIER.get());
            require(siegeEngineer != null && fieldMedic != null && cavalier != null,
                    "One or more military specialist guard modules are missing");

            tower.setBuildingLevel(1);
            require(siegeEngineer.getModuleMax() == 1
                            && fieldMedic.getModuleMax() == 1
                            && cavalier.getModuleMax() == 1,
                    "Level 1 Guard Tower must have a one-guard total capacity");

            final ICitizenData engineerData = colony.getCitizenManager().createAndRegisterCivilianData();
            require(siegeEngineer.assignCitizen(engineerData),
                    "Level 1 Guard Tower refused its first Siege Engineer");
            require(engineerData.getJob() != null
                            && engineerData.getJob().getJobRegistryEntry()
                                    .equals(ModImperiumJobs.IMPERIAL_SIEGE_ENGINEER.get()),
                    "MineColonies did not assign the registered Siege Engineer job");
            require(!fieldMedic.assignCitizen(colony.getCitizenManager().createAndRegisterCivilianData()),
                    "Level 1 Guard Tower accepted a second guard beyond its shared capacity");

            tower.setBuildingLevel(5);
            require(siegeEngineer.getModuleMax() == 5
                            && fieldMedic.getModuleMax() == 5
                            && cavalier.getModuleMax() == 5,
                    "Level 5 Guard Tower must scale the shared capacity to five guards");

            final ICitizenData medicData = colony.getCitizenManager().createAndRegisterCivilianData();
            require(fieldMedic.assignCitizen(medicData),
                    "Level 5 Guard Tower refused its first Imperial Field Medic");
            require(medicData.getJob() != null
                            && medicData.getJob().getJobRegistryEntry()
                                    .equals(ModImperiumJobs.IMPERIAL_FIELD_MEDIC.get()),
                    "MineColonies did not assign the registered Imperial Field Medic job");

            final ICitizenData cavalierData = colony.getCitizenManager().createAndRegisterCivilianData();
            require(cavalier.assignCitizen(cavalierData),
                    "Level 5 Guard Tower refused its first Imperial Cavalier");
            require(cavalierData.getJob() != null
                            && cavalierData.getJob().getJobRegistryEntry()
                                    .equals(ModImperiumJobs.IMPERIAL_CAVALIER.get()),
                    "MineColonies did not assign the registered Imperial Cavalier job");

            require(siegeEngineer.assignCitizen(colony.getCitizenManager().createAndRegisterCivilianData()),
                    "Level 5 Guard Tower refused a second Siege Engineer below capacity");
            require(fieldMedic.assignCitizen(colony.getCitizenManager().createAndRegisterCivilianData()),
                    "Level 5 Guard Tower refused a second Field Medic below capacity");
            require(tower.getAllAssignedCitizen().size() == 5,
                    "Level 5 Guard Tower did not reach its five-guard shared capacity");
            require(!cavalier.assignCitizen(colony.getCitizenManager().createAndRegisterCivilianData()),
                    "Level 5 Guard Tower accepted a sixth guard beyond capacity");

            LOGGER.info("IMPERIUM_GAMETEST_PASS: real Guard Tower hut registration, military specialist jobs and level-scaled shared capacity");
        } finally {
            IColonyManager.getInstance().deleteColonyByWorld(colony.getID(), false, level);
        }

        helper.succeed();
    }

    private static GuardBuildingModule findGuardModule(
            final java.util.List<GuardBuildingModule> modules,
            final com.minecolonies.api.colony.jobs.registry.JobEntry entry) {
        return modules.stream()
                .filter(module -> module.getJobEntry().equals(entry))
                .findFirst()
                .orElse(null);
    }

    private static WorkerBuildingModule findWorkerModule(
            final java.util.List<WorkerBuildingModule> modules,
            final com.minecolonies.api.colony.jobs.registry.JobEntry entry) {
        return modules.stream()
                .filter(module -> module.getJobEntry().equals(entry))
                .findFirst()
                .orElse(null);
    }

    private static int uniqueTestColonyId() {
        // These are synthetic identity keys only; no MineColonies colony is created or mutated.
        final int positiveBits = (int) (UUID.randomUUID().getMostSignificantBits() & 0x3fffffffL);
        return 1_000_000_000 + positiveBits;
    }

    private static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
