package com.imperium.realms.block;

import com.imperium.realms.registry.ModImperiumBuildings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/** MineColonies hut anchor for the Imperial Guard Tower. */
public final class BlockHutImperialGuardTower
        extends AbstractBlockHut<BlockHutImperialGuardTower> {

    @Override
    public String getHutName() {
        return "blockhutimperialguardtower";
    }

    @Override
    public BuildingEntry getBuildingEntry() {
        return ModImperiumBuildings.IMPERIAL_GUARD_TOWER.get();
    }
}
