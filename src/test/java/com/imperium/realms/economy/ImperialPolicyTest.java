package com.imperium.realms.economy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ImperialPolicyTest {
    @Test
    void policyIdsAndRatesAreStable() {
        assertEquals(1, ImperialPolicy.LOW_TAX.crownsPerCitizenPerDay());
        assertEquals(2, ImperialPolicy.BALANCED.crownsPerCitizenPerDay());
        assertEquals(4, ImperialPolicy.EMERGENCY_LEVY.crownsPerCitizenPerDay());
        assertEquals(ImperialPolicy.LOW_TAX, ImperialPolicy.fromId("LOW_TAX").orElseThrow());
        assertTrue(ImperialPolicy.fromId("unknown").isEmpty());
        assertTrue(ImperialPolicy.fromId(null).isEmpty());
    }
}
