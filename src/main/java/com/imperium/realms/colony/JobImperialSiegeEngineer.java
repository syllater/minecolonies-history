package com.imperium.realms.colony;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.guard.JobKnight;

/**
 * Imperial siege specialist. Uses MineColonies' native knight combat AI and
 * equipment flow; level-ups additionally record siege-engineering experience.
 */
public final class JobImperialSiegeEngineer extends JobKnight {
    public JobImperialSiegeEngineer(final ICitizenData citizenData) {
        super(citizenData);
    }

    @Override
    public void onLevelUp() {
        super.onLevelUp();
        MilitaryTrainingRecorder.record(
                getColony(), EmpireState.MilitaryDiscipline.SIEGE_ENGINEERING);
    }
}
