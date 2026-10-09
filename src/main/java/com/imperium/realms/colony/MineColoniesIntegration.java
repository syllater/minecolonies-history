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
     * Reconcile known MineColonies colonies and advance the daily treasury and
     * political simulation. The game-day guard in SavedData makes multi-dimension
     * scans safe and idempotent.
     *
     * @return number of new Imperium records created during this scan.
     */
    public static int synchronizeLoadedColonies(final ServerLevel level) {
        Objects.requireNonNull(level, "level");
        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final long gameTime = level.getServer().overworld().getGameTime();
        final long gameDay = Math.floorDiv(gameTime, 24_000L);
        int created = 0;

        for (final IColony colony : IMinecoloniesAPI.getInstance()
                .getColonyManager()
                .getColonies(level)) {
            if (colony == null) {
                continue;
            }
            final ColonyIdentity identity = ColonyIdentity.from(colony);
            if (data.observeColony(identity, colony.getName(), gameTime)) {
                created++;
            }

            // Day zero is the onboarding day. Automatic taxes and political
            // developments begin after the first full day in an established colony.
            if (gameDay > 0L) {
                data.collectTaxes(identity, colony.getCitizenManager().getCurrentCitizenCount(), gameTime);
                data.processPoliticalDay(identity, colony.getOverallHappiness(), gameDay);
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
        data.observeColony(identity, colony.getName(), level.getServer().overworld().getGameTime());
        return data.get(identity).orElseThrow(
                () -> new IllegalStateException("Empire state was not created for " + identity.storageKey()));
    }
}
