package com.imperium.realms.colony;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ImperialSupplyRouteTest {
    @Test
    void routeProcessesUpkeepAtMostOncePerGameDay() {
        final ColonyIdentity destination = new ColonyIdentity("minecraft:overworld", 25);
        final ImperialSupplyRoute route = ImperialSupplyRoute.restore(destination, 4L, 100, 4L);

        assertTrue(route.isActive());
        assertFalse(route.processDay(4L, false));
        assertTrue(route.processDay(5L, true));
        assertEquals(100, route.condition());
        assertFalse(route.processDay(5L, false));
        assertTrue(route.processDay(9L, false));
        assertEquals(90, route.condition());
        assertEquals(9L, route.lastUpkeepDay());
    }

    @Test
    void unpaidRouteBecomesInactiveAndPaidRouteRepairsIt() {
        final ColonyIdentity destination = new ColonyIdentity("minecraft:overworld", 26);
        final ImperialSupplyRoute route = ImperialSupplyRoute.restore(destination, 0L, 45, 0L);

        assertTrue(route.processDay(1L, false));
        assertEquals(35, route.condition());
        assertFalse(route.isActive());
        for (long day = 2L; day <= 8L; day++) {
            assertTrue(route.processDay(day, false));
        }
        assertEquals(0, route.condition());
        assertTrue(route.processDay(9L, true));
        assertEquals(5, route.condition());
    }

    @Test
    void restoreRejectsInvalidTiming() {
        final ColonyIdentity destination = new ColonyIdentity("minecraft:overworld", 27);
        assertThrows(IllegalArgumentException.class,
                () -> ImperialSupplyRoute.restore(destination, 9L, 100, 8L));
    }
}
