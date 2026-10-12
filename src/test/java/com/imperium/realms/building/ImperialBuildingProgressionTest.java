package com.imperium.realms.building;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ImperialBuildingProgressionTest {
    @Test
    void workerLimitMatchesEachSupportedTier() {
        for (int level = 1; level <= ImperialBuildingProgression.MAX_BUILDING_LEVEL; level++) {
            assertEquals(level, ImperialBuildingProgression.workerLimitForLevel(level));
        }
    }

    @Test
    void unbuiltAndInvalidLevelsCannotCreateWorkersOrExceedCap() {
        assertEquals(0, ImperialBuildingProgression.workerLimitForLevel(-1));
        assertEquals(0, ImperialBuildingProgression.workerLimitForLevel(0));
        assertEquals(5, ImperialBuildingProgression.workerLimitForLevel(6));
        assertEquals(5, ImperialBuildingProgression.workerLimitForLevel(Integer.MAX_VALUE));
    }
}
