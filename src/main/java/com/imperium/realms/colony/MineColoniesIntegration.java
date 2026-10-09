package com.imperium.realms.colony;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.Optional;

/**
 * Thin adapter over MineColonies' public API.
 *
 * <p>All mutation and persistence is server-side. Client-only colony views are
 * intentionally not used as an authority for empire state.</p>
 */
public final class MineColoniesIntegration {
    private MineColoniesIntegration() {
    }

    public static Optional<IColony> colonyAt(final ServerLevel level, final BlockPos position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");

        return Optional.ofNullable(
                IMinecoloniesAPI.getInstance()
                        .getColonyManager()
                        .getColonyByPosFromWorld(level, position));
    }

    /**
     * Reconcile colonies already known to MineColonies with Imperium's own
     * persistence. Safe to call repeatedly; existing records are not reset.
     *
     * @return number of new Imperium records created during this scan.
     */
    public static int synchronizeLoadedColonies(final ServerLevel level) {
        Objects.requireNonNull(level, "level");
        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        int created = 0;

        for (final IColony colony : IMinecoloniesAPI.getInstance()
                .getColonyManager()
                .getColonies(level)) {
            if (colony == null) {
                continue;
            }
            final ColonyIdentity identity = ColonyIdentity.from(colony);
            if (data.observeColony(identity, colony.getName(), level.getGameTime())) {
                created++;
            }
        }
        return created;
    }

    /**
     * Return the existing state for a colony, creating it on first observation.
     */
    public static EmpireState getOrCreateState(final ServerLevel level, final IColony colony) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(colony, "colony");

        final ColonyIdentity identity = ColonyIdentity.from(colony);
        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        data.observeColony(identity, colony.getName(), level.getGameTime());
        return data.get(identity).orElseThrow(
                () -> new IllegalStateException("Empire state was not created for " + identity.storageKey()));
    }
}
