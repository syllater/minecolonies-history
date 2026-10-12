package com.imperium.realms.block;

import com.imperium.realms.registry.ModImperiumBuildings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Native MineColonies hut anchor for the Imperial Archive.
 *
 * <p>The block participates in MineColonies' colony building tile-entity and
 * registry flow. The matching Structurize schematics are still authored
 * separately; see docs/MILESTONE_3.md.</p>
 */
public final class BlockHutImperialArchive extends AbstractBlockHut<BlockHutImperialArchive> {
    @Override
    public String getHutName() {
        return "blockhutimperialarchive";
    }

    @Override
    public BuildingEntry getBuildingEntry() {
        return ModImperiumBuildings.IMPERIAL_ARCHIVE.get();
    }
}
