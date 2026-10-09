package com.imperium.realms.colony;

import com.imperium.realms.economy.ImperialPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmpireStateEconomicTest {
    @Test
    void balancedTaxesAreCollectedOnlyOncePerDay() {
        final EmpireState state = EmpireState.create(new ColonyIdentity("minecraft:overworld", 22), "Capital", 0L);
        assertEquals(0L, state.collectDailyTax(23_999L, 5));
        assertEquals(10L, state.collectDailyTax(24_000L, 5));
        assertEquals(0L, state.collectDailyTax(24_001L, 5));
        assertEquals(10L, state.treasuryCrowns());
        assertEquals(1L, state.lastEconomyDay());
    }

    @Test
    void policyChangesTheNextDailyTax() {
        final EmpireState state = EmpireState.create(new ColonyIdentity("minecraft:overworld", 23), "Harbour", 0L);
        assertTrue(state.setPolicy(ImperialPolicy.LOW_TAX));
        assertFalse(state.setPolicy(ImperialPolicy.LOW_TAX));
        assertEquals(3L, state.collectDailyTax(24_000L, 3));
        assertTrue(state.setPolicy(ImperialPolicy.EMERGENCY_LEVY));
        assertEquals(12L, state.collectDailyTax(48_000L, 3));
        assertEquals(15L, state.treasuryCrowns());
    }

    @Test
    void diplomatCreatesAtMostOneInfluencePerDay() {
        final EmpireState state = EmpireState.create(new ColonyIdentity("minecraft:overworld", 24), "Port", 0L);
        assertTrue(state.recordDiplomaticWork(0L));
        assertFalse(state.recordDiplomaticWork(1L));
        assertTrue(state.recordDiplomaticWork(24_000L));
        assertEquals(2L, state.diplomaticInfluence());
        assertEquals(1L, state.lastDiplomaticDay());
    }

    @Test
    void treasuryPreventsNegativeBalancesAndOverflow() {
        final EmpireState state = EmpireState.create(new ColonyIdentity("minecraft:overworld", 25), "Market", 0L);
        assertFalse(state.spendCrowns(1L));
        assertTrue(state.creditCrowns(40L));
        assertTrue(state.spendCrowns(15L));
        assertEquals(25L, state.treasuryCrowns());
        assertFalse(state.creditCrowns(Long.MAX_VALUE));
        assertFalse(state.creditCrowns(0L));
        assertEquals(25L, state.treasuryCrowns());
    }
}
