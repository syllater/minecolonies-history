package com.imperium.realms.colony;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ColonyIdentityTest {
    @Test
    void identityUsesDimensionAndColonyId() {
        final ColonyIdentity overworldColony = new ColonyIdentity("minecraft:overworld", 7);
        final ColonyIdentity sameColony = new ColonyIdentity("minecraft:overworld", 7);
        final ColonyIdentity differentId = new ColonyIdentity("minecraft:overworld", 8);
        final ColonyIdentity differentDimension = new ColonyIdentity("minecraft:the_nether", 7);

        assertEquals(overworldColony, sameColony);
        assertEquals("minecraft:overworld#7", overworldColony.storageKey());
        assertNotEquals(overworldColony, differentId);
        assertNotEquals(overworldColony, differentDimension);
    }

    @Test
    void rejectsInvalidIdentity() {
        assertThrows(NullPointerException.class, () -> new ColonyIdentity(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new ColonyIdentity("", 1));
        assertThrows(IllegalArgumentException.class, () -> new ColonyIdentity("overworld", 1));
        assertThrows(IllegalArgumentException.class, () -> new ColonyIdentity("minecraft:overworld", -1));
    }
}
