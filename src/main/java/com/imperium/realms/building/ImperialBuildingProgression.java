package com.imperium.realms.building;

/**
 * Shared progression rules for Imperium's original MineColonies huts.
 *
 * <p>MineColonies supplies the current hut level to each module. Clamp it to
 * the supported five-tier range so a corrupt/unusual level cannot create an
 * unbounded worker limit, while level 0 still permits no worker slots.</p>
 */
public final class ImperialBuildingProgression {
    public static final int MAX_BUILDING_LEVEL = 5;

    private ImperialBuildingProgression() {
    }

    /**
     * Worker/guard slot count for the given building level.
     *
     * @param buildingLevel current MineColonies building level
     * @return a slot count in the inclusive range 0..5
     */
    public static int workerLimitForLevel(final int buildingLevel) {
        return Math.max(0, Math.min(MAX_BUILDING_LEVEL, buildingLevel));
    }
}
