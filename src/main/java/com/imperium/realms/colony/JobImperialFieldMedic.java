package com.imperium.realms.colony;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.guard.JobDruid;

/**
 * Imperial field medic. Inherits MineColonies' native druid support/combat AI
 * and potion handling while maintaining separate field-medicine progression.
 */
public final class JobImperialFieldMedic extends JobDruid {
    public JobImperialFieldMedic(final ICitizenData citizenData) {
        super(citizenData);
    }

    @Override
    public void onLevelUp() {
        super.onLevelUp();
        MilitaryTrainingRecorder.record(
                getColony(), EmpireState.MilitaryDiscipline.FIELD_MEDICINE);
    }
}
