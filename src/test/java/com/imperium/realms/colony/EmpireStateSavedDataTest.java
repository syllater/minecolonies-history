package com.imperium.realms.colony;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
