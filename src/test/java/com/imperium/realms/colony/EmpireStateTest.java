package com.imperium.realms.colony;

import com.imperium.realms.economy.EmpirePolicy;
import com.imperium.realms.politics.FactionType;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmpireStateTest {
    @Test
    void firstObservationUsesSafeDefaults() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "  Capital  ", -9);

        assertEquals(identity, state.identity());
        assertEquals("Capital", state.colonyName());
        assertEquals(0L, state.firstSeenGameTime());
        assertEquals(0L, state.lastSeenGameTime());
        assertEquals(0L, state.treasury());
        assertEquals(10, state.taxRatePercent());
        assertEquals(EmpirePolicy.BALANCED, state.policy());
        assertEquals(50, state.citizenApproval());
        assertEquals(0, state.unrest());
        assertEquals(100, state.factionSupportSnapshot().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void observationUpdatesNameWithoutResettingFirstSeen() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 100L);

        assertTrue(state.observe("New Capital", 200L));
        assertEquals("New Capital", state.colonyName());
        assertEquals(100L, state.firstSeenGameTime());
        assertEquals(100L, state.lastSeenGameTime());
    }

    @Test
    void heartbeatOnlyAdvancesOnAnInGameDayBoundary() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 100L);

        assertFalse(state.observe("Capital", 23_999L));
        assertEquals(100L, state.lastSeenGameTime());

        assertTrue(state.observe("Capital", 24_100L));
        assertEquals(24_100L, state.lastSeenGameTime());
        assertEquals(100L, state.firstSeenGameTime());
    }

    @Test
    void emptyNamesAreNormalized() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "", 10L);
        assertEquals("Unnamed colony", state.colonyName());
    }

    @Test
    void taxesAreCollectedOncePerGameDayWithPolicyUpkeep() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 0L);
        assertTrue(state.setPolicy(EmpirePolicy.PUBLIC_WORKS));

        final EmpireState.TaxCollectionResult first = state.collectTaxes(10, 24_000L).orElseThrow();
        assertEquals(100L, first.grossRevenue());
        assertEquals(20L, first.upkeepDue());
        assertEquals(20L, first.upkeepPaid());
        assertEquals(80L, first.treasuryBalance());
        assertEquals(80L, first.netChange());

        assertEquals(Optional.empty(), state.collectTaxes(10, 24_500L));
        assertEquals(80L, state.treasury());

        final EmpireState.TaxCollectionResult second = state.collectTaxes(10, 48_000L).orElseThrow();
        assertEquals(2L, second.gameDay());
        assertEquals(160L, second.treasuryBalance());
    }

    @Test
    void staffedTaxCollectorsImproveRevenueAndEffectiveRateIsCapped() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 0L);

        final EmpireState.TaxCollectionResult staffed = state.collectTaxes(10, 2, 24_000L).orElseThrow();
        assertEquals(200L, staffed.grossRevenue());
        assertEquals(200L, staffed.treasuryBalance());

        final EmpireState capped = EmpireState.create(identity, "Other Capital", 0L);
        capped.setTaxRatePercent(50);
        final EmpireState.TaxCollectionResult maxStaffed = capped.collectTaxes(10, 99, 24_000L).orElseThrow();
        assertEquals(750L, maxStaffed.grossRevenue());
        assertEquals(750L, maxStaffed.treasuryBalance());
    }

    @Test
    void policyUpkeepNeverMakesTreasuryNegative() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 0L);
        state.setTaxRatePercent(0);
        state.setPolicy(EmpirePolicy.SCHOLARSHIP);

        final EmpireState.TaxCollectionResult result = state.collectTaxes(5, 24_000L).orElseThrow();
        assertEquals(15L, result.upkeepDue());
        assertEquals(0L, result.upkeepPaid());
        assertEquals(15L, result.unpaidUpkeep());
        assertEquals(0L, result.treasuryBalance());
    }

    @Test
    void taxRateIsRestrictedToSafeRange() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 0L);

        assertThrows(IllegalArgumentException.class, () -> state.setTaxRatePercent(-1));
        assertThrows(IllegalArgumentException.class, () -> state.setTaxRatePercent(51));
        assertTrue(state.setTaxRatePercent(25));
        assertFalse(state.setTaxRatePercent(25));
    }

    @Test
    void politicsIsProcessedOncePerDayAndImprovesWithHighHappiness() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 0L);

        assertTrue(state.processPoliticsDay(8.0, 0L).isEmpty());
        final EmpireState.PoliticalReport report = state.processPoliticsDay(8.0, 1L).orElseThrow();
        assertEquals(80, report.happinessPercent());
        assertTrue(report.approval() > report.previousApproval());
        assertTrue(state.processPoliticsDay(2.0, 1L).isEmpty());
    }

    @Test
    void policyAndTaxRateShiftFactionSupportWithoutChangingTheTotal() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 0L);
        assertTrue(state.setPolicy(EmpirePolicy.PUBLIC_WORKS));
        assertTrue(state.setTaxRatePercent(30));

        state.processPoliticsDay(5.5, 1L).orElseThrow();
        final Map<FactionType, Integer> support = state.factionSupportSnapshot();

        assertEquals(100, support.values().stream().mapToInt(Integer::intValue).sum());
        assertTrue(support.get(FactionType.COMMONERS_ASSEMBLY) > 25);
        assertTrue(support.values().stream().allMatch(value -> value >= 0 && value <= 100));
    }

    @Test
    void factionSupportRestoresToAValidDistribution() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = new EmpireState(
                identity, "Capital", 0, 0, 0, 10, EmpirePolicy.BALANCED, -1,
                50, 0, 0, Map.of(FactionType.CROWN_LOYALISTS, 80));

        assertEquals(100, state.factionSupportSnapshot().values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(50, state.citizenApproval());
    }
}
