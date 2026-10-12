package com.imperium.realms.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingGuardTower;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Native MineColonies guard building hosting Imperium's three specialist
 * guard roles. A matching Structurize schematic pack is still required for
 * survival construction and upgrades.
 */
public final class BuildingImperialGuardTower extends BuildingGuardTower {
    public static final String SCHEMATIC_NAME = "imperialguardtower";

    public BuildingImperialGuardTower(final IColony colony, final BlockPos position) {
        super(colony, position);
    }

    @NotNull
    @Override
    public String getSchematicName() {
        return SCHEMATIC_NAME;
    }

    @Override
    public int getMaxBuildingLevel() {
        return 5;
    }

    /** Client view for the imperial tower, with MineColonies guard management screens. */
    public static class View extends BuildingGuardTower.View {
        public View(final com.minecolonies.api.colony.IColonyView colony, @NotNull final BlockPos position) {
            super(colony, position);
        }
    }
}
