package com.imperium.realms.colony;

import com.minecolonies.api.colony.IColony;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

/**
 * Bridges MineColonies guard level-ups into Imperium-owned persisted military
 * records. Training yield depends on the assigned Guard Tower level (1/2/3 at
 * tiers 1–2/3–4/5) and is doubled by the province's Military focus. Training
 * remains server-side and each discipline is capped by EmpireState.
 */
public final class MilitaryTrainingRecorder {
    private MilitaryTrainingRecorder() {
    }

    public static void record(
            final IColony colony,
            final EmpireState.MilitaryDiscipline discipline) {
        record(colony, discipline, 1);
    }

    public static void record(
            final IColony colony,
            final EmpireState.MilitaryDiscipline discipline,
            final int guardTowerLevel) {
        if (colony == null) {
            return;
        }
        Objects.requireNonNull(discipline, "discipline");
        if (!(colony.getWorld() instanceof ServerLevel level)) {
            return;
        }

        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final EmpireState state = MineColoniesIntegration.getOrCreateState(level, colony);
        if (state.recordMilitaryTraining(discipline, guardTowerLevel)) {
            data.markChanged();
        }
    }
}
