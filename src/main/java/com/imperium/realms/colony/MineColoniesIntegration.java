package com.imperium.realms.colony;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.Optional;

/** Thin server-authoritative adapter over MineColonies' public API. */
public final class MineColoniesIntegration {
    private MineColoniesIntegration() { }

    public static Optional<IColony> colonyAt(final ServerLevel level, final BlockPos position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        return Optional.ofNullable(IMinecoloniesAPI.getInstance().getColonyManager()
                .getColonyByPosFromWorld(level, position));
    }

    /** Reconcile loaded colonies; repeat scans never reset existing records. */
    public static int synchronizeLoadedColonies(final ServerLevel level) {
        Objects.requireNonNull(level, "level");
        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final long gameTime = level.getServer().overworld().getGameTime();
        int created = 0;
        for (final IColony colony : IMinecoloniesAPI.getInstance().getColonyManager().getColonies(level)) {
            if (colony == null) continue;
            final ColonyIdentity identity = ColonyIdentity.from(colony);
            if (data.observeColony(identity, colony.getName(), gameTime)) created++;
            data.collectDailyTax(identity, gameTime, colony.getCitizenManager().getCurrentCitizenCount());
        }
        return created;
    }

    public static EmpireState getOrCreateState(final ServerLevel level, final IColony colony) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(colony, "colony");
        final ColonyIdentity identity = ColonyIdentity.from(colony);
        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        data.observeColony(identity, colony.getName(), level.getServer().overworld().getGameTime());
        return data.get(identity).orElseThrow(
                () -> new IllegalStateException("Empire state was not created for " + identity.storageKey()));
    }

    public static boolean recordDiplomaticWork(final ServerLevel level, final IColony colony) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(colony, "colony");
        return EmpireStateSavedData.get(level).recordDiplomaticWork(
                ColonyIdentity.from(colony), colony.getName(), level.getServer().overworld().getGameTime());
    }
}
