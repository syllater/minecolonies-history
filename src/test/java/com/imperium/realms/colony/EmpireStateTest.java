package com.imperium.realms.colony;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmpireStateTest {
    @Test
    void firstObservationCreatesDefaultStateWithStableFirstSeenTime() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 42);
        final EmpireState state = EmpireState.create(identity, "New Rome", 1200L);

        assertEquals(identity, state.identity());
        assertEquals("New Rome", state.colonyName());
        assertEquals(1200L, state.firstSeenGameTime());
        assertEquals(1200L, state.lastSeenGameTime());
    }

    @Test
    void observationUpdatesRenamedColonyWithoutResettingFirstSeenTime() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 42), "Old Name", 100L);

        assertTrue(state.observe("New Name", 200L));
        assertEquals("New Name", state.colonyName());
        assertEquals(100L, state.firstSeenGameTime());
        assertEquals(100L, state.lastSeenGameTime());
    }

    @Test
    void heartbeatAdvancesOnlyAtTheNextInGameDayBoundary() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 42), "New Rome", 100L);

        assertFalse(state.observe("New Rome", 23_999L));
        assertEquals(100L, state.lastSeenGameTime());

        assertTrue(state.observe("New Rome", 24_000L));
        assertEquals(24_000L, state.lastSeenGameTime());
        assertEquals(100L, state.firstSeenGameTime());
    }

    @Test
    void invalidOrBlankColonyNameGetsASafeDisplayName() {
        final EmpireState nullName = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 1), null, 0L);
        final EmpireState blankName = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 2), "   ", 0L);

        assertEquals("Unnamed colony", nullName.colonyName());
        assertEquals("Unnamed colony", blankName.colonyName());
    }
}
