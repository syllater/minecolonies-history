package com.imperium.realms;

import com.imperium.realms.ai.DiplomatAI;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;

/** MineColonies citizen job for the imperial Diplomat profession. */
public final class JobDiplomat extends AbstractJob<DiplomatAI, JobDiplomat> {
    public JobDiplomat(final ICitizenData citizenData) { super(citizenData); }
    @Override public DiplomatAI generateAI() { return new DiplomatAI(this); }
}
