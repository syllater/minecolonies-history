package com.imperium.realms.ai;

import com.imperium.realms.building.BuildingImperialArchive;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.JobTaxCollector;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.minecolonies.api.entity.ai.JobStatus;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.IStateSupplier;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;

/**
 * The Tax Collector works at the Imperial Archive and periodically files tax
 * records. Each successful filing improves the colony's collection efficiency,
 * up to the persisted cap; the daily economy applies that bonus on the server.
 */
public final class EntityAIWorkTaxCollector
        extends AbstractEntityAIInteract<JobTaxCollector, BuildingImperialArchive> {

    public EntityAIWorkTaxCollector(final JobTaxCollector job) {
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
        if (!canFileRecords() || !shouldWorkNow()) {
            markIdle();
            return AIWorkerState.IDLE;
        }
        markWorking();
        return walkToBuilding() ? AIWorkerState.START_WORKING : AIWorkerState.PREPARING;
    }

    private IAIState work() {
        if (!canFileRecords() || !shouldWorkNow()) {
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
            if (state.recordTaxCollectorWork(world.getGameTime())) {
                data.markChanged();
            }
        }
        return AIWorkerState.START_WORKING;
    }

    private boolean canFileRecords() {
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
