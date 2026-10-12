package com.imperium.realms.colony;

import com.imperium.realms.ai.EntityAIWorkPhilosopher;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;

/** First custom profession in the Imperium/MineColonies integration. */
public final class JobPhilosopher extends AbstractJob<EntityAIWorkPhilosopher, JobPhilosopher> {
    public JobPhilosopher(final ICitizenData citizenData) {
        super(citizenData);
    }

    @Override
    public EntityAIWorkPhilosopher generateAI() {
        return new EntityAIWorkPhilosopher(this);
    }
}
