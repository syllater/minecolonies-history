package com.imperium.realms;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/** Server-side MineColonies building implementation for the Imperial Chancery. */
public final class ImperialChancery extends AbstractBuilding {
    public ImperialChancery(@NotNull final IColony colony, @NotNull final BlockPos position) {
        super(colony, position);
    }

    @Override public String getSchematicName() { return "imperial_chancery"; }

    /** MineColonies supplies its standard BlockUI worker assignment window. */
    public static final class View extends AbstractBuildingView {
        public View(final IColonyView colonyView, @NotNull final BlockPos position) {
            super(colonyView, position);
        }
    }
}
