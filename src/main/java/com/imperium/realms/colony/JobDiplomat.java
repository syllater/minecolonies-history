package com.imperium.realms.colony;

import com.imperium.realms.ai.EntityAIWorkDiplomat;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;

/** MineColonies profession that builds diplomatic influence for its colony. */
public final class JobDiplomat extends AbstractJob<EntityAIWorkDiplomat, JobDiplomat> {
    public JobDiplomat(final ICitizenData citizenData) {
        super(citizenData);
    }

    @Override
    public EntityAIWorkDiplomat generateAI() {
        return new EntityAIWorkDiplomat(this);
    }
}
