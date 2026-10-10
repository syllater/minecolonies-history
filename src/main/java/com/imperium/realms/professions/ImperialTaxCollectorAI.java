package com.imperium.realms.professions;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.ai.workers.AbstractAISkeleton;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.IDLE;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.START_WORKING;

/**
 * Lightweight administrative work loop for a Tax Collector citizen.
 *
 * <p>Colony-wide accounting is performed by the server simulation. This AI
 * ensures MineColonies assigns, persists and ticks the worker as a real job;
 * staffing is read by the daily economy update for its revenue bonus.</p>
 */
public final class ImperialTaxCollectorAI extends AbstractAISkeleton<ImperialTaxCollectorJob> {
    public ImperialTaxCollectorAI(final ImperialTaxCollectorJob job) {
        super(job);
        registerTargets(
                new AITarget(IDLE, START_WORKING, 1),
                new AITarget(START_WORKING, this::processImperialAccounts, Constants.TICKS_SECOND));
    }

    private IAIState processImperialAccounts() {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        return IDLE;
    }
}
