package com.imperium.realms.ai;

import com.imperium.realms.ImperialChancery;
import com.imperium.realms.JobDiplomat;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

/** Navigates to the Chancery and produces one diplomatic influence point per day. */
public final class DiplomatAI extends AbstractEntityAIInteract<JobDiplomat, ImperialChancery> {
    public DiplomatAI(@NotNull final JobDiplomat job) {
        super(job);
        super.registerTargets(
                new AITarget<>(AIWorkerState.IDLE, AIWorkerState.START_WORKING, 1),
                new AITarget<IAIState>(AIWorkerState.START_WORKING, this::startWorkingAtChancery, 20),
                new AITarget<IAIState>(AIWorkerState.DECIDE, this::performDiplomaticWork, 240));
    }

    @Override
    public Class<ImperialChancery> getExpectedBuildingClass() {
        return ImperialChancery.class;
    }

    private IAIState startWorkingAtChancery() {
        if (!walkToBuilding()) return getState();
        return AIWorkerState.DECIDE;
    }

    private IAIState performDiplomaticWork() {
        if (building != null && building.getColony().getWorld() instanceof ServerLevel serverLevel) {
            MineColoniesIntegration.recordDiplomaticWork(serverLevel, building.getColony());
        }
        return AIWorkerState.IDLE;
    }
}
