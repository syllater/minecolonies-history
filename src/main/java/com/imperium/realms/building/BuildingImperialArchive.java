package com.imperium.realms.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;

/**
 * MineColonies server-side building object for the Imperial Archive.
 */
public final class BuildingImperialArchive extends AbstractBuilding {
    public static final String SCHEMATIC_NAME = "imperialarchive";

    public BuildingImperialArchive(final IColony colony, final BlockPos position) {
        super(colony, position);
    }

    @Override
    public String getSchematicName() {
        return SCHEMATIC_NAME;
    }

    /**
     * The initial slice supports one schematic tier. This remains deliberately
     * limited until the level 1 Structurize blueprint is included and tested.
     */
    @Override
    public int getMaxBuildingLevel() {
        return 1;
    }
}
