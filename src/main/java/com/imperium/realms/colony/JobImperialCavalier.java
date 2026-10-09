package com.imperium.realms.colony;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.guard.JobCavalry;

/**
 * Imperial cavalry role. Inherits MineColonies' cavalier AI, mount state and
 * equipment handling, then records separate cavalry drill on each level-up.
 */
public final class JobImperialCavalier extends JobCavalry {
    public JobImperialCavalier(final ICitizenData citizenData) {
        super(citizenData);
    }

    @Override
    public void onLevelUp() {
        super.onLevelUp();
        MilitaryTrainingRecorder.record(
                getColony(), EmpireState.MilitaryDiscipline.CAVALRY_DRILL);
    }
}
