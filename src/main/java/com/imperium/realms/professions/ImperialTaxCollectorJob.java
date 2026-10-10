package com.imperium.realms.professions;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.jobs.AbstractJob;
import org.jetbrains.annotations.NotNull;

/**
 * MineColonies job assigned by the Imperial Tax Collector hut.
 */
public final class ImperialTaxCollectorJob extends AbstractJob<ImperialTaxCollectorAI, ImperialTaxCollectorJob> {
    public ImperialTaxCollectorJob(final ICitizenData citizenData) {
        super(citizenData);
    }

    @NotNull
    @Override
    public ImperialTaxCollectorAI generateAI() {
        return new ImperialTaxCollectorAI(this);
    }
}
