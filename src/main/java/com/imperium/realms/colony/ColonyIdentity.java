package com.imperium.realms.colony;

import com.minecolonies.api.colony.IColony;

import java.util.Objects;

/**
 * Stable identity for Imperium data attached to a MineColonies colony.
 *
 * <p>Colony names and center positions can change; the dimension plus MineColonies
 * colony ID identifies the same colony across reloads.</p>
 */
public record ColonyIdentity(String dimensionId, int colonyId) {
    public ColonyIdentity {
        Objects.requireNonNull(dimensionId, "dimensionId");
        if (dimensionId.isBlank() || !dimensionId.contains(":")) {
            throw new IllegalArgumentException("dimensionId must be a namespaced dimension key");
        }
        if (colonyId < 0) {
            throw new IllegalArgumentException("colonyId must not be negative");
        }
    }

    public static ColonyIdentity from(final IColony colony) {
        Objects.requireNonNull(colony, "colony");
        return new ColonyIdentity(
                colony.getWorld().dimension().location().toString(),
                colony.getID());
    }

    /**
     * A compact deterministic key useful in logs and serialized maps.
     */
    public String storageKey() {
        return dimensionId + "#" + colonyId;
    }
}
