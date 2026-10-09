package com.imperium.realms;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/** MineColonies hut anchor for the Imperial Chancery. */
public final class ImperialChanceryBlock extends AbstractBlockHut<ImperialChanceryBlock> {
    @Override public String getHutName() { return "imperial_chancery"; }
    @Override public BuildingEntry getBuildingEntry() { return ImperialBuildingRegistry.IMPERIAL_CHANCERY.get(); }
}
