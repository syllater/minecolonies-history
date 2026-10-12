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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    @GameTest(template = "flat", timeoutTicks = 100)
    public static void nonOwnerCannotMutateColonyEconomyThroughCommands(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final ServerPlayer owner = FakePlayerFactory.get(level, COLONY_TEST_OWNER);
        final BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        owner.setPos(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D);
        final IColony colony = IColonyManager.getInstance().createColony(
                level, center, owner, "Imperium Permission Test", "Colonial");
        require(colony != null, "MineColonies did not create the command-permission test colony");

        try {
            final GameProfile outsiderProfile = new GameProfile(
                    UUID.fromString("7e50ba59-8e88-40d8-b5e4-3d7734c63e87"),
                    "imperium_outsider");
            final ServerPlayer outsider = FakePlayerFactory.get(level, outsiderProfile);
            outsider.setPos(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D);

            final EmpireStateSavedData data = EmpireStateSavedData.get(level);
            final ColonyIdentity identity = ColonyIdentity.from(colony);
            final EmpireState state = MineColoniesIntegration.getOrCreateState(level, colony);

            // Prove the command route itself is valid: the colony owner can submit
            // a bill through the same Brigadier dispatcher used by real players.
            final int ownerTaxResult = executeCommand(level, owner, "imperium tax 10");
            require(ownerTaxResult > 0, "The colony owner should be able to submit a tax proposal");
            require(state.recentParliamentProposals().size() == 1,
                    "The authorized tax command did not create exactly one proposal");
            final long originalProposalId = state.recentParliamentProposals().get(0).id();

            // Realm setup and a funded provincial treasury let this test exercise real
            // treasury command paths as well as the basic tax/policy authorization.
            final int foundResult = executeCommand(level, owner, "imperium empire found Permission Realm");
            require(foundResult > 0, "The colony owner should be able to found the test realm");
            require(data.realmForProvince(identity).isPresent(),
                    "Authorized realm creation did not register its capital");
            final long openingProvinceTreasury = state.treasuryCrowns();
            require(state.creditTreasury(250L), "Could not fund the test province treasury");
            final long fundedProvinceTreasury = state.treasuryCrowns();
            require(fundedProvinceTreasury == openingProvinceTreasury + 250L,
                    "Test setup did not add exactly 250 crowns to the province");
            data.markChanged();

            final int ownerDepositResult = executeCommand(level, owner, "imperium empire deposit 50");
            require(ownerDepositResult > 0, "The realm owner should be able to deposit into the imperial treasury");
            final EmpireState provinceAfterDeposit = data.get(identity).orElseThrow();
            final EmpireRealm realmAfterDeposit = data.realmForProvince(identity).orElseThrow();
            LOGGER.info("IMPERIUM_GAMETEST_DIAGNOSTIC: after owner deposit provincial={} imperial={}",
                    provinceAfterDeposit.treasuryCrowns(), realmAfterDeposit.imperialTreasuryCrowns());
            require(provinceAfterDeposit.treasuryCrowns() == fundedProvinceTreasury - 50L,
                    "Authorized deposit left provincial treasury at " + provinceAfterDeposit.treasuryCrowns()
                            + " crowns; expected " + (fundedProvinceTreasury - 50L));
            require(realmAfterDeposit.imperialTreasuryCrowns() == 50L,
                    "Authorized deposit left imperial treasury at " + realmAfterDeposit.imperialTreasuryCrowns()
                            + " crowns; expected 50");

            // A second player standing in the same colony must not be able to use
            // proximity alone to change policy, province funds or the imperial reserve.
            final int outsiderTaxResult = executeCommand(level, outsider, "imperium tax 15");
            require(outsiderTaxResult == 0,
                    "A non-owner without MANAGE_HUTS must not submit a tax proposal");
            final int outsiderPolicyResult = executeCommand(level, outsider, "imperium policy mercantile");
            require(outsiderPolicyResult == 0,
                    "A non-owner without MANAGE_HUTS must not submit an economic policy proposal");
            final int outsiderDepositResult = executeCommand(level, outsider, "imperium empire deposit 25");
            require(outsiderDepositResult == 0,
                    "A non-owner without MANAGE_HUTS must not transfer provincial funds");
            final int outsiderWithdrawResult = executeCommand(level, outsider, "imperium empire withdraw 10");
            require(outsiderWithdrawResult == 0,
                    "A non-Emperor must not withdraw funds from the imperial reserve");
            require(executeCommand(level, outsider,
                            "imperium parliament assent " + originalProposalId) == 0,
                    "A non-owner must not assent to a parliamentary bill");
            require(executeCommand(level, outsider,
                            "imperium parliament veto " + originalProposalId) == 0,
                    "A non-owner must not veto a parliamentary bill");
            final ParliamentProposal proposalAfterDeniedVotes =
                    state.findProposal(originalProposalId).orElseThrow();
            require(proposalAfterDeniedVotes.status() == ParliamentProposal.Status.OPEN
                            && proposalAfterDeniedVotes.resolvedBy().isBlank(),
                    "Denied parliamentary vote commands changed the bill status or resolution actor");

            require(data.get(identity).orElseThrow().treasuryCrowns() == fundedProvinceTreasury - 50L
                            && data.realmForProvince(identity).orElseThrow().imperialTreasuryCrowns() == 50L,
                    "Denied treasury commands changed provincial or imperial balances");
            final var afterDeniedCommands = state.recentParliamentProposals();
            require(afterDeniedCommands.size() == 1 && afterDeniedCommands.get(0).id() == originalProposalId,
                    "Unauthorized commands changed the stored parliament proposals");

            // The legitimate Emperor can still resolve the same bill after the
            // outsider's denied assent/veto attempts.
            executeCommand(level, owner, "imperium parliament veto " + originalProposalId);
            final ParliamentProposal resolvedProposal = state.findProposal(originalProposalId).orElseThrow();
            require(resolvedProposal.status() == ParliamentProposal.Status.REJECTED
                            && owner.getGameProfile().getName().equals(resolvedProposal.resolvedBy()),
                    "The Emperor's veto did not resolve the open bill under the Emperor's identity");

            // The legitimate Emperor remains able to withdraw; this guards against
            // a test that only passes because all treasury actions are broken.
            final int ownerWithdrawResult = executeCommand(level, owner, "imperium empire withdraw 10");
            require(ownerWithdrawResult > 0, "The Emperor should be able to withdraw from the imperial treasury");
            final long provinceAfterWithdrawal = data.get(identity).orElseThrow().treasuryCrowns();
            final long imperialAfterWithdrawal = data.realmForProvince(identity).orElseThrow().imperialTreasuryCrowns();
            require(provinceAfterWithdrawal == fundedProvinceTreasury - 40L && imperialAfterWithdrawal == 40L,
                    "Authorized withdrawal balances were provincial=" + provinceAfterWithdrawal
                            + ", imperial=" + imperialAfterWithdrawal + "; expected "
                            + (fundedProvinceTreasury - 40L) + "/40");

            final EmpireRealm realmBeforeRejectedTransfers = data.realmForProvince(identity).orElseThrow();
            final int auditCountBeforeRejectedTransfers = realmBeforeRejectedTransfers.recentAuditEntries().size();
            final long provinceBeforeRejectedTransfers = data.get(identity).orElseThrow().treasuryCrowns();
            final long reserveBeforeRejectedTransfers = realmBeforeRejectedTransfers.imperialTreasuryCrowns();

            // These amounts fit the command's numeric range but exceed the available
            // provincial/imperial balances. The authoritative command path must reject
            // them without leaking funds or appending success audit entries.
            final int insufficientDepositResult = executeCommand(
                    level, owner, "imperium empire deposit 1000000000");
            require(insufficientDepositResult == 0,
                    "A deposit exceeding the provincial balance must be rejected");
            final int insufficientWithdrawalResult = executeCommand(
                    level, owner, "imperium empire withdraw 1000000000");
            require(insufficientWithdrawalResult == 0,
                    "A withdrawal exceeding the imperial reserve must be rejected");

            final long provinceAfterRejectedTransfers = data.get(identity).orElseThrow().treasuryCrowns();
            final EmpireRealm realmAfterRejectedTransfers = data.realmForProvince(identity).orElseThrow();
            require(provinceAfterRejectedTransfers == provinceBeforeRejectedTransfers
                            && realmAfterRejectedTransfers.imperialTreasuryCrowns() == reserveBeforeRejectedTransfers,
                    "Insufficient-funds transfers changed a treasury balance");
            require(realmAfterRejectedTransfers.recentAuditEntries().size() == auditCountBeforeRejectedTransfers,
                    "Insufficient-funds transfers added audit entries");

            LOGGER.info("IMPERIUM_GAMETEST_PASS: owner tax/treasury/parliament actions succeed; non-owner tax/policy/treasury/assent/veto mutations are denied; insufficient-funds transfers preserve balances and audit history");
        } finally {
            IColonyManager.getInstance().deleteColonyByWorld(colony.getID(), false, level);
        }

        helper.succeed();
    }

    @GameTest(template = "flat", timeoutTicks = 100)
    public static void crossDimensionRealmInvitationRequiresProvinceOwner(final GameTestHelper helper) {
        final ServerLevel storageLevel = helper.getLevel();
        final ServerLevel capitalLevel = storageLevel.getServer().getLevel(Level.END);
        final ServerLevel provinceLevel = storageLevel.getServer().getLevel(Level.NETHER);
        require(capitalLevel != null, "The server did not expose the End level");
        require(provinceLevel != null, "The server did not expose the Nether level");

        final GameProfile capitalProfile = new GameProfile(
                UUID.randomUUID(), "imperium_realm_capital");
        final GameProfile provinceProfile = new GameProfile(
                UUID.randomUUID(), "imperium_realm_province");
        final GameProfile outsiderProfile = new GameProfile(
                UUID.randomUUID(), "imperium_realm_outsider");

        final BlockPos capitalCenter = new BlockPos(0, 64, 0);
        capitalLevel.getChunkAt(capitalCenter);
        final ServerPlayer capitalOwner = FakePlayerFactory.get(capitalLevel, capitalProfile);
        capitalOwner.setPos(capitalCenter.getX() + 0.5D, capitalCenter.getY(), capitalCenter.getZ() + 0.5D);
        IColony capitalColony = IColonyManager.getInstance().createColony(
                capitalLevel, capitalCenter, capitalOwner, "Imperium Cross-Dimension Capital", "Colonial");
        require(capitalColony != null, "MineColonies did not create the test capital");

        IColony provinceColony = null;
        try {
            final BlockPos provinceCenter = new BlockPos(0, 64, 0);
            provinceLevel.getChunkAt(provinceCenter);
            final ServerPlayer provinceOwner = FakePlayerFactory.get(provinceLevel, provinceProfile);
            provinceOwner.setPos(provinceCenter.getX() + 0.5D,
                    provinceCenter.getY(), provinceCenter.getZ() + 0.5D);
            provinceColony = IColonyManager.getInstance().createColony(
                    provinceLevel, provinceCenter, provinceOwner, "Imperium Cross-Dimension Province", "Colonial");
            require(provinceColony != null, "MineColonies did not create the Nether province");

            final EmpireStateSavedData data = EmpireStateSavedData.get(storageLevel);
            final ColonyIdentity capitalIdentity = ColonyIdentity.from(capitalColony);
            final ColonyIdentity provinceIdentity = ColonyIdentity.from(provinceColony);
            require(!capitalIdentity.dimensionId().equals(provinceIdentity.dimensionId()),
                    "The test colonies must belong to different dimensions");

            require(executeCommand(capitalLevel, capitalOwner, "imperium empire found Cross-Dimension Realm") > 0,
                    "The capital owner could not found a realm");
            final EmpireRealm realm = data.realmForProvince(capitalIdentity).orElseThrow(
                    () -> new AssertionError("The capital was not added to the new realm"));

            final String inviteCommand = "imperium empire invite "
                    + provinceIdentity.dimensionId() + " " + provinceIdentity.colonyId();
            require(executeCommand(capitalLevel, capitalOwner, inviteCommand) > 0,
                    "The Emperor could not invite a province in another dimension");
            require(realm.invitations().containsKey(provinceIdentity),
                    "The cross-dimension invitation was not stored against the full colony identity");

            final ServerPlayer outsider = FakePlayerFactory.get(provinceLevel, outsiderProfile);
            outsider.setPos(provinceCenter.getX() + 0.5D,
                    provinceCenter.getY(), provinceCenter.getZ() + 0.5D);
            require(executeCommand(provinceLevel, outsider, "imperium empire join") == 0,
                    "A non-owner must not accept a colony invitation based on proximity alone");
            require(data.realmForProvince(provinceIdentity).isEmpty(),
                    "An outsider's rejected join command changed province membership");
            require(realm.invitations().containsKey(provinceIdentity),
                    "A rejected join attempt must not consume the valid invitation");

            require(executeCommand(provinceLevel, provinceOwner, "imperium empire join") > 0,
                    "The invited colony owner could not accept the realm invitation");
            final EmpireRealm provinceRealm = data.realmForProvince(provinceIdentity).orElseThrow(
                    () -> new AssertionError("The invited colony was not added to the realm"));
            require(provinceRealm.id() == realm.id() && provinceRealm.provinceCount() == 2,
                    "Realm membership did not preserve both the capital and remote province");
            require(provinceRealm.capital().equals(capitalIdentity),
                    "Accepting an invitation changed the realm's capital identity");

            require(executeCommand(provinceLevel, provinceOwner, "imperium empire join") == 0,
                    "A province that already joined must not join the same realm a second time");
            require(data.realmForProvince(provinceIdentity).orElseThrow().provinceCount() == 2,
                    "A duplicate join attempt changed realm membership");

            final CompoundTag snapshot = data.save(new CompoundTag(), storageLevel.registryAccess());
            final EmpireStateSavedData restored =
                    EmpireStateSavedData.load(snapshot, storageLevel.registryAccess());
            final EmpireRealm restoredRealm = restored.realmForProvince(provinceIdentity).orElseThrow(
                    () -> new AssertionError("Cross-dimension realm membership did not survive NBT reload"));
            require(restoredRealm.id() == realm.id()
                            && restoredRealm.capital().equals(capitalIdentity)
                            && restoredRealm.containsProvince(provinceIdentity)
                            && restoredRealm.provinceCount() == 2,
                    "Cross-dimension capital/province identity changed after NBT round-trip");

            LOGGER.info("IMPERIUM_GAMETEST_PASS: real cross-dimension colony invitation, owner-only join, duplicate-join denial and realm NBT round-trip");
        } finally {
            if (provinceColony != null) {
                IColonyManager.getInstance().deleteColonyByWorld(
                        provinceColony.getID(), false, provinceLevel);
            }
            if (capitalColony != null) {
                IColonyManager.getInstance().deleteColonyByWorld(
                        capitalColony.getID(), false, capitalLevel);
            }
        }

        helper.succeed();
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

    private static int executeCommand(
            final ServerLevel level,
            final ServerPlayer player,
            final String command) {
        try {
            return level.getServer().getCommands().getDispatcher()
                    .execute(command, player.createCommandSourceStack());
        } catch (CommandSyntaxException exception) {
            throw new AssertionError("Could not execute GameTest command: " + command, exception);
        }
    }

    private static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
