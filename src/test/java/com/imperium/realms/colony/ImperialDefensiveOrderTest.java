package com.imperium.realms.colony;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ImperialDefensiveOrderTest {
    @Test
    void orderHasBoundedDurationAndReadinessBonus() {
        final ColonyIdentity province = new ColonyIdentity("minecraft:overworld", 41);
        final ImperialDefensiveOrder order = ImperialDefensiveOrder.issue(province, "Emperor", 10L);

        assertEquals(10L, order.issuedDay());
        assertEquals(17L, order.expiresDay());
        assertTrue(order.isActive(10L));
        assertTrue(order.isActive(16L));
        assertFalse(order.isActive(17L));
        assertEquals(20, ImperialDefensiveOrder.READINESS_BONUS);
    }

    @Test
    void restoredOrderRejectsInvalidTiming() {
        final ColonyIdentity province = new ColonyIdentity("minecraft:overworld", 42);
        assertThrows(IllegalArgumentException.class,
                () -> ImperialDefensiveOrder.restore(province, "Emperor", 5L, 4L));
    }
}
