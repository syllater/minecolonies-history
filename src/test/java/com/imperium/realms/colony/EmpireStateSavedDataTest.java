package com.imperium.realms.colony;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmpireStateSavedDataTest {
    @Test
    void economyAndProfessionProgressSurviveNbtRoundTrip() {
        final EmpireStateSavedData original = new EmpireStateSavedData();
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 8);
        assertTrue(original.observeColony(identity, "Old Settlement Name", 100L));

        final EmpireState state = original.get(identity).orElseThrow();
        assertTrue(state.creditTreasury(777L));
        assertTrue(state.setTaxRatePercent(15));
        assertTrue(state.setEconomicPolicy(EconomicPolicy.MERCANTILE));
        assertTrue(state.recordScholarWork(1_200L));
        assertTrue(state.recordTaxCollectorWork(1_200L));
        assertTrue(state.recordDiplomatWork(2_400L));
        assertTrue(state.setProvinceFocus(ProvinceFocus.MILITARY));
        assertTrue(state.recordMilitaryTraining(EmpireState.MilitaryDiscipline.SIEGE_ENGINEERING));
        state.restorePoliticalSimulation(
                Map.of("merchants", 61, "commons", 37, "nobility", 54, "scholars", 83),
                22, EmpireState.CivicDisorder.CALM, 1L);

        final CompoundTag saved = original.save(new CompoundTag(), null);
        final EmpireStateSavedData restored = EmpireStateSavedData.load(saved, null);
        final EmpireState loaded = restored.get(identity).orElseThrow();

        assertEquals("Old Settlement Name", loaded.colonyName());
        assertEquals(100L, loaded.firstSeenGameTime());
        assertEquals(777L, loaded.treasuryCrowns());
        assertEquals(15, loaded.taxRatePercent());
        assertEquals(EconomicPolicy.MERCANTILE, loaded.economicPolicy());
        assertEquals(1L, loaded.knowledgePoints());
        assertEquals(1, loaded.taxCollectionEfficiencyPercent());
        assertEquals(1L, loaded.diplomaticInfluence());
        assertEquals(ProvinceFocus.MILITARY, loaded.provinceFocus());
        assertEquals(2L, loaded.siegeEngineeringPoints());
        assertEquals(61, loaded.factionApproval("merchants"));
        assertEquals(37, loaded.factionApproval("commons"));
        assertEquals(54, loaded.factionApproval("nobility"));
        assertEquals(83, loaded.factionApproval("scholars"));
        assertEquals(22, loaded.unrest());
        assertEquals(EmpireState.CivicDisorder.CALM, loaded.civicDisorder());
    }

    @Test
    void tierScaledProfessionAndGuardTrainingSurviveNbtRoundTrip() {
        final EmpireStateSavedData original = new EmpireStateSavedData();
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 9);
        assertTrue(original.observeColony(identity, "Tier Five Province", 0L));

        final EmpireState state = original.get(identity).orElseThrow();
        assertTrue(state.setProvinceFocus(ProvinceFocus.MILITARY));
        assertTrue(state.recordScholarWork(1_200L, 5, "philosopher-a"));
        assertTrue(state.recordTaxCollectorWork(1_200L, 5, "tax-collector-a"));
        assertTrue(state.recordDiplomatWork(2_400L, 5, "diplomat-a"));
        assertTrue(state.recordMilitaryTraining(EmpireState.MilitaryDiscipline.FIELD_MEDICINE, 5));

        assertEquals(3L, state.knowledgePoints());
        assertEquals(3, state.taxCollectionEfficiencyPercent());
        assertEquals(3L, state.diplomaticInfluence());
        assertEquals(6L, state.fieldMedicinePoints());

        final EmpireStateSavedData restored = EmpireStateSavedData.load(
                original.save(new CompoundTag(), null), null);
        final EmpireState loaded = restored.get(identity).orElseThrow();

        assertEquals(3L, loaded.knowledgePoints());
        assertEquals(3, loaded.taxCollectionEfficiencyPercent());
        assertEquals(1_200L, loaded.lastScholarWorkTick());
        assertEquals(1_200L, loaded.lastTaxCollectorWorkTick());
        assertEquals(3L, loaded.diplomaticInfluence());
        assertEquals(2_400L, loaded.lastDiplomatWorkTick());
        assertEquals(ProvinceFocus.MILITARY, loaded.provinceFocus());
        assertEquals(6L, loaded.fieldMedicinePoints());

        // Loading a world must not permit an immediate duplicate work cycle.
        assertFalse(loaded.recordScholarWork(1_200L, 5, "philosopher-a"));
        assertFalse(loaded.recordTaxCollectorWork(1_200L, 5, "tax-collector-a"));
        assertFalse(loaded.recordDiplomatWork(2_400L, 5, "diplomat-a"));

        // A different citizen still works in the same interval after reload.
        assertTrue(loaded.recordScholarWork(1_200L, 5, "philosopher-b"));
        assertTrue(loaded.recordTaxCollectorWork(1_200L, 5, "tax-collector-b"));
        assertTrue(loaded.recordDiplomatWork(2_400L, 5, "diplomat-b"));
        assertEquals(6L, loaded.knowledgePoints());
        assertEquals(6, loaded.taxCollectionEfficiencyPercent());
        assertEquals(6L, loaded.diplomaticInfluence());
    }

    @Test
    void realmLawsMembershipRoutesDefensesAndGovernorsSurviveRoundTrip() {
        final EmpireStateSavedData original = new EmpireStateSavedData();
        final ColonyIdentity capital = new ColonyIdentity("minecraft:overworld", 1);
        final ColonyIdentity province = new ColonyIdentity("minecraft:the_nether", 4);
        assertTrue(original.observeColony(capital, "Capital", 0L));
        assertTrue(original.observeColony(province, "Border Province", 0L));

        final EmpireRealm realm = original.createRealm(
                "North Sea Union", capital, "emperor-uuid", "Emperor", 0L).orElseThrow();
        assertTrue(realm.setImperialTaxRatePercent(12));
        assertTrue(realm.setImperialEconomicPolicy(EconomicPolicy.WELFARE));
        assertTrue(original.inviteProvince(realm.id(), province, 1L));
        assertEquals(realm.id(), original.acceptRealmInvitation(province, 1L).orElseThrow().id());

        assertTrue(realm.depositImperialTreasury(500L));
        assertTrue(realm.buildSupplyRoute(province, 2L, "Emperor"));
        assertTrue(realm.issueDefensiveOrder(province, "Emperor", 2L));
        assertTrue(realm.appointGovernor(province, "governor-uuid", "Governor", 2L));
        realm.recordAudit(2L, "Emperor", "test-checkpoint", "province", 1L);

        final EmpireStateSavedData restored = EmpireStateSavedData.load(
                original.save(new CompoundTag(), null), null);

        final EmpireRealm loadedRealm = restored.realmById(realm.id()).orElseThrow();
        assertEquals(2, loadedRealm.provinceCount());
        assertTrue(loadedRealm.containsProvince(capital));
        assertTrue(loadedRealm.containsProvince(province));
        assertEquals("North Sea Union", loadedRealm.name());
        assertEquals("emperor-uuid", loadedRealm.emperorUuid());
        assertEquals(12, loadedRealm.imperialTaxRatePercent());
        assertEquals("welfare", loadedRealm.imperialEconomicPolicyId());
        assertEquals(350L, loadedRealm.imperialTreasuryCrowns());
        assertTrue(loadedRealm.supplyRouteTo(province).orElseThrow().isActive());
        assertEquals(ImperialDefensiveOrder.READINESS_BONUS,
                loadedRealm.defensiveOrderBonus(province, 3L));
        assertEquals("Governor", loadedRealm.governorFor(province).orElseThrow().playerName());
        assertEquals("test-checkpoint", loadedRealm.recentAuditEntries().get(0).actionId());

        final EmpireState loadedProvince = restored.get(province).orElseThrow();
        assertEquals(12, loadedProvince.taxRatePercent());
        assertEquals(EconomicPolicy.WELFARE, loadedProvince.economicPolicy());
    }

    @Test
    void realmMembershipComparisonIncludesDifferentDimensions() {
        final EmpireStateSavedData data = new EmpireStateSavedData();
        final ColonyIdentity capital = new ColonyIdentity("minecraft:overworld", 1);
        final ColonyIdentity crossDimensionMember = new ColonyIdentity("minecraft:the_nether", 4);
        final ColonyIdentity independent = new ColonyIdentity("minecraft:the_nether", 9);
        assertTrue(data.observeColony(capital, "Capital", 0L));
        assertTrue(data.observeColony(crossDimensionMember, "Border Province", 0L));
        assertTrue(data.observeColony(independent, "Independent Province", 0L));

        final EmpireRealm realm = data.createRealm(
                "Cross-Dimension Union", capital, "emperor-uuid", "Emperor", 0L).orElseThrow();
        assertTrue(data.inviteProvince(realm.id(), crossDimensionMember, 1L));
        assertTrue(data.acceptRealmInvitation(crossDimensionMember, 1L).isPresent());

        assertTrue(data.areProvincesInSameRealm(capital, crossDimensionMember));
        assertTrue(data.areProvincesInSameRealm(crossDimensionMember, capital));
        assertFalse(data.areProvincesInSameRealm(capital, independent));
        assertFalse(data.areProvincesInSameRealm(independent, crossDimensionMember));
        assertFalse(data.areProvincesInSameRealm(null, capital));
        assertFalse(data.areProvincesInSameRealm(capital, null));
    }

    @Test
    void olderSchemaMissingNewFieldsMigratesWithSafeDefaults() {
        final CompoundTag root = new CompoundTag();
        root.putInt("schema_version", 1);
        final ListTag colonies = new ListTag();
        final CompoundTag oldColony = new CompoundTag();
        oldColony.putString("dimension", "minecraft:overworld");
        oldColony.putInt("colony_id", 21);
        oldColony.putString("colony_name", "Legacy Settlement");
        oldColony.putLong("first_seen", 12L);
        oldColony.putLong("last_seen", 24L);
        colonies.add(oldColony);
        root.put("colonies", colonies);

        final EmpireStateSavedData migrated = EmpireStateSavedData.load(root, null);
        final EmpireState state = migrated.get(
                new ColonyIdentity("minecraft:overworld", 21)).orElseThrow();

        assertEquals("Legacy Settlement", state.colonyName());
        assertEquals(12L, state.firstSeenGameTime());
        assertEquals(24L, state.lastSeenGameTime());
        assertEquals(0L, state.treasuryCrowns());
        assertEquals(5, state.taxRatePercent());
        assertEquals(EconomicPolicy.BALANCED, state.economicPolicy());
        assertEquals(0L, state.knowledgePoints());
        assertEquals(50, state.stability());
        assertEquals(50, state.legitimacy());
        assertEquals(ProvinceFocus.AGRICULTURE, state.provinceFocus());
        assertEquals(0L, state.diplomaticInfluence());
    }

    @Test
    void malformedColonyRecordIsSkippedWithoutDiscardingValidNeighbor() {
        final CompoundTag root = new CompoundTag();
        root.putInt("schema_version", 22);
        final ListTag colonies = new ListTag();

        final CompoundTag malformed = new CompoundTag();
        malformed.putString("dimension", "not-a-namespaced-id");
        malformed.putInt("colony_id", 99);
        malformed.putString("colony_name", "Malformed");
        colonies.add(malformed);

        final CompoundTag missingPath = new CompoundTag();
        missingPath.putString("dimension", "minecraft:");
        missingPath.putInt("colony_id", 98);
        missingPath.putString("colony_name", "Missing path");
        colonies.add(missingPath);

        final CompoundTag invalidPath = new CompoundTag();
        invalidPath.putString("dimension", "minecraft:the nether");
        invalidPath.putInt("colony_id", 97);
        invalidPath.putString("colony_name", "Invalid path");
        colonies.add(invalidPath);

        final CompoundTag nonCanonical = new CompoundTag();
        nonCanonical.putString("dimension", "Minecraft:Overworld");
        nonCanonical.putInt("colony_id", 96);
        nonCanonical.putString("colony_name", "Non-canonical identifier");
        colonies.add(nonCanonical);

        final CompoundTag valid = new CompoundTag();
        valid.putString("dimension", "minecraft:overworld");
        valid.putInt("colony_id", 22);
        valid.putString("colony_name", "Valid Neighbor");
        valid.putLong("first_seen", 50L);
        valid.putLong("last_seen", 75L);
        colonies.add(valid);
        root.put("colonies", colonies);

        final EmpireStateSavedData loaded = EmpireStateSavedData.load(root, null);

        assertEquals(1, loaded.allStates().size());
        assertEquals("Valid Neighbor", loaded.get(
                new ColonyIdentity("minecraft:overworld", 22)).orElseThrow().colonyName());
    }


    @Test
    void treasuryDepositTransfersFundsAtomicallyAndAuditsOnlySuccess() {
        final TreasuryFixture fixture = treasuryFixture(250L, 20L);

        assertEquals(EmpireStateSavedData.TreasuryTransferResult.TRANSFERRED,
                fixture.data().depositIntoImperialTreasury(
                        fixture.province(), 100L, "Emperor", 3L));
        assertEquals(150L, fixture.provinceState().treasuryCrowns());
        assertEquals(120L, fixture.realm().imperialTreasuryCrowns());
        assertEquals("deposit", fixture.realm().recentAuditEntries().get(0).actionId());

        final EmpireStateSavedData reloaded = EmpireStateSavedData.load(
                fixture.data().save(new CompoundTag(), null), null);
        assertEquals(150L, reloaded.get(fixture.province()).orElseThrow().treasuryCrowns());
        final EmpireRealm reloadedRealm = reloaded.realmForProvince(fixture.province()).orElseThrow();
        assertEquals(120L, reloadedRealm.imperialTreasuryCrowns());
        assertEquals("deposit", reloadedRealm.recentAuditEntries().get(0).actionId());

        assertEquals(EmpireStateSavedData.TreasuryTransferResult.INSUFFICIENT_PROVINCE_FUNDS,
                fixture.data().depositIntoImperialTreasury(
                        fixture.province(), 200L, "Emperor", 4L));
        assertEquals(150L, fixture.provinceState().treasuryCrowns());
        assertEquals(120L, fixture.realm().imperialTreasuryCrowns());
        assertEquals(1, fixture.realm().recentAuditEntries().size(),
                "failed transfers must not create misleading audit entries");
    }

    @Test
    void treasuryDepositDoesNotDebitProvinceWhenImperialReserveIsFull() {
        final TreasuryFixture fixture = treasuryFixture(
                50L, EmpireRealm.MAX_IMPERIAL_TREASURY - 5L);

        assertEquals(EmpireStateSavedData.TreasuryTransferResult.IMPERIAL_TREASURY_FULL,
                fixture.data().depositIntoImperialTreasury(
                        fixture.province(), 10L, "Emperor", 5L));
        assertEquals(50L, fixture.provinceState().treasuryCrowns());
        assertEquals(EmpireRealm.MAX_IMPERIAL_TREASURY - 5L,
                fixture.realm().imperialTreasuryCrowns());
        assertTrue(fixture.realm().recentAuditEntries().isEmpty());
    }

    @Test
    void treasuryWithdrawalIsAtomicForSuccessInsufficientReserveAndProvinceCap() {
        final TreasuryFixture success = treasuryFixture(100L, 120L);
        assertEquals(EmpireStateSavedData.TreasuryTransferResult.TRANSFERRED,
                success.data().withdrawFromImperialTreasury(
                        success.province(), 40L, "Emperor", 6L));
        assertEquals(140L, success.provinceState().treasuryCrowns());
        assertEquals(80L, success.realm().imperialTreasuryCrowns());
        assertEquals("withdraw", success.realm().recentAuditEntries().get(0).actionId());

        final TreasuryFixture insufficient = treasuryFixture(0L, 5L);
        assertEquals(EmpireStateSavedData.TreasuryTransferResult.INSUFFICIENT_IMPERIAL_FUNDS,
                insufficient.data().withdrawFromImperialTreasury(
                        insufficient.province(), 10L, "Emperor", 6L));
        assertEquals(0L, insufficient.provinceState().treasuryCrowns());
        assertEquals(5L, insufficient.realm().imperialTreasuryCrowns());
        assertTrue(insufficient.realm().recentAuditEntries().isEmpty());

        final TreasuryFixture fullProvince = treasuryFixture(EmpireState.MAX_TREASURY - 5L, 20L);
        assertEquals(EmpireStateSavedData.TreasuryTransferResult.PROVINCE_TREASURY_FULL,
                fullProvince.data().withdrawFromImperialTreasury(
                        fullProvince.province(), 10L, "Emperor", 6L));
        assertEquals(EmpireState.MAX_TREASURY - 5L, fullProvince.provinceState().treasuryCrowns());
        assertEquals(20L, fullProvince.realm().imperialTreasuryCrowns());
        assertTrue(fullProvince.realm().recentAuditEntries().isEmpty());
    }

    private static TreasuryFixture treasuryFixture(
            final long provinceTreasury,
            final long imperialTreasury) {
        final EmpireStateSavedData data = new EmpireStateSavedData();
        final ColonyIdentity capital = new ColonyIdentity("minecraft:overworld", 1);
        final ColonyIdentity province = new ColonyIdentity("minecraft:the_nether", 4);
        assertTrue(data.observeColony(capital, "Capital", 0L));
        assertTrue(data.observeColony(province, "Border Province", 0L));

        final EmpireState state = data.get(province).orElseThrow();
        if (provinceTreasury > 0L) {
            assertTrue(state.creditTreasury(provinceTreasury));
        }

        final EmpireRealm realm = data.createRealm(
                "North Sea Union", capital, "emperor-uuid", "Emperor", 0L).orElseThrow();
        assertTrue(data.inviteProvince(realm.id(), province, 1L));
        assertTrue(data.acceptRealmInvitation(province, 1L).isPresent());
        if (imperialTreasury > 0L) {
            assertTrue(realm.depositImperialTreasury(imperialTreasury));
        }
        return new TreasuryFixture(data, state, realm, province);
    }

    private record TreasuryFixture(
            EmpireStateSavedData data,
            EmpireState provinceState,
            EmpireRealm realm,
            ColonyIdentity province) {
    }

}
