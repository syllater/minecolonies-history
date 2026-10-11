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

}
