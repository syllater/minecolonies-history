package com.imperium.realms.colony;

import com.imperium.realms.ImperiumRealms;
import com.mojang.logging.LogUtils;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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
