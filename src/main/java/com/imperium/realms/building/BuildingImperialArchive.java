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

    /** The generated Structurize pack provides all five upgrade tiers. */
    @Override
    public int getMaxBuildingLevel() {
        return 5;
    }
}
