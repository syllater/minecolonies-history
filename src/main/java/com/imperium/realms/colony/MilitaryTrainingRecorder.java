package com.imperium.realms.colony;

import com.minecolonies.api.colony.IColony;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

/** Bridges MineColonies guard level-ups into Imperium-owned persisted military records. */
public final class MilitaryTrainingRecorder {
    private MilitaryTrainingRecorder() {
    }

    public static void record(
            final IColony colony,
            final EmpireState.MilitaryDiscipline discipline) {
        if (colony == null) {
            return;
        }
        Objects.requireNonNull(discipline, "discipline");
        if (!(colony.getWorld() instanceof ServerLevel level)) {
            return;
        }

        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final EmpireState state = MineColoniesIntegration.getOrCreateState(level, colony);
        if (state.recordMilitaryTraining(discipline)) {
            data.markChanged();
        }
    }
}
