package com.imperium.realms.colony;

import com.minecolonies.api.colony.IColony;
import net.minecraft.resources.ResourceLocation;

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

        // A colon alone is not enough to make a valid Minecraft identifier. Check
        // the resource-location grammar here so malformed NBT cannot create keys
        // that will never resolve through MineColonies' dimension lookup.
        try {
            final ResourceLocation parsed = ResourceLocation.parse(dimensionId);
            if (!parsed.toString().equals(dimensionId)) {
                throw new IllegalArgumentException("dimensionId must be canonical: " + dimensionId);
            }
        } catch (final RuntimeException invalidResourceLocation) {
            if (invalidResourceLocation instanceof IllegalArgumentException illegal
                    && illegal.getMessage() != null
                    && illegal.getMessage().startsWith("dimensionId must be canonical:")) {
                throw illegal;
            }
            throw new IllegalArgumentException("dimensionId must be a valid namespaced dimension key: " + dimensionId,
                    invalidResourceLocation);
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
