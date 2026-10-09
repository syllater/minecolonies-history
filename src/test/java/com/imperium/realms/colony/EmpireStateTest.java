package com.imperium.realms.colony;

import com.imperium.realms.economy.EmpirePolicy;
import org.junit.jupiter.api.Test;

import java.util.Optional;

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
}
