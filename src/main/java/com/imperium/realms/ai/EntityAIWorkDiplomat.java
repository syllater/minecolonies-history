package com.imperium.realms.ai;

import com.imperium.realms.building.BuildingImperialArchive;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.JobDiplomat;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.minecolonies.api.entity.ai.JobStatus;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.IStateSupplier;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;

/**
 * A Diplomat works from the Imperial Archive and prepares dispatches. Each
 * successful work interval grants one diplomatic influence point, up to the
 * colony's persisted cap.
 */
public final class EntityAIWorkDiplomat
        extends AbstractEntityAIInteract<JobDiplomat, BuildingImperialArchive> {

    public EntityAIWorkDiplomat(final JobDiplomat job) {
        super(job);
        registerTargets(
                new AITarget<>(AIWorkerState.PREPARING,
                        (IStateSupplier<IAIState>) this::prepare, 20),
                new AITarget<>(AIWorkerState.START_WORKING,
                        (IStateSupplier<IAIState>) this::work, 20),
                new AITarget<>(AIWorkerState.IDLE,
                        (IStateSupplier<IAIState>) this::idleState, 20));
    }

    @Override
    public Class<BuildingImperialArchive> getExpectedBuildingClass() {
        return BuildingImperialArchive.class;
    }

    private IAIState idleState() {
        if (shouldWorkNow()) {
            markWorking();
            return AIWorkerState.PREPARING;
        }
        markIdle();
        return AIWorkerState.IDLE;
    }

    private IAIState prepare() {
        if (!canPrepareDispatches() || !shouldWorkNow()) {
            markIdle();
            return AIWorkerState.IDLE;
        }
        markWorking();
        return walkToBuilding() ? AIWorkerState.START_WORKING : AIWorkerState.PREPARING;
    }

    private IAIState work() {
        if (!canPrepareDispatches() || !shouldWorkNow()) {
            markIdle();
            return AIWorkerState.IDLE;
        }
        markWorking();
        if (!walkToBuilding()) {
            return AIWorkerState.START_WORKING;
        }

        if (job.getColony() != null && world != null) {
            final EmpireStateSavedData data = EmpireStateSavedData.get(world);
            final var state = MineColoniesIntegration.getOrCreateState(world, job.getColony());
            if (state.recordDiplomatWork(world.getGameTime())) {
                data.markChanged();
            }
        }
        return AIWorkerState.START_WORKING;
    }

    private boolean canPrepareDispatches() {
        return building != null && worker != null && worker.isAlive();
    }

    private boolean shouldWorkNow() {
        return world == null || world.isDay();
    }

    private void markWorking() {
        if (worker != null && worker.getCitizenData() != null) {
            worker.getCitizenData().setJobStatus(JobStatus.WORKING);
            worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        }
    }

    private void markIdle() {
        if (worker != null && worker.getCitizenData() != null) {
            worker.getCitizenData().setJobStatus(JobStatus.IDLE);
        }
    }
}
