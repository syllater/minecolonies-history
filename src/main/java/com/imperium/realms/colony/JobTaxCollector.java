package com.imperium.realms.colony;

import com.imperium.realms.ai.EntityAIWorkTaxCollector;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;

/** MineColonies profession whose work improves a colony's tax collection efficiency. */
public final class JobTaxCollector extends AbstractJob<EntityAIWorkTaxCollector, JobTaxCollector> {
    public JobTaxCollector(final ICitizenData citizenData) {
        super(citizenData);
    }

    @Override
    public EntityAIWorkTaxCollector generateAI() {
        return new EntityAIWorkTaxCollector(this);
    }
}
