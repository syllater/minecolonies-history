package com.imperium.realms.ai;

import com.imperium.realms.building.BuildingImperialArchive;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.JobPhilosopher;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.IStateSupplier;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;

/**
 * The Philosopher works at the Imperial Archive. After reaching its work hut,
 * the worker records a research point once per in-game minute.
 */
public final class EntityAIWorkPhilosopher
        extends AbstractEntityAIInteract<JobPhilosopher, BuildingImperialArchive> {

    public EntityAIWorkPhilosopher(final JobPhilosopher job) {
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

    @Override
    protected IAIState decide() {
        return canStudy() ? AIWorkerState.PREPARING : AIWorkerState.IDLE;
    }

    private IAIState prepare() {
        if (!canStudy()) {
            markIdle();
            return AIWorkerState.IDLE;
        }
        markWorking();
        return walkToBuilding() ? AIWorkerState.START_WORKING : AIWorkerState.PREPARING;
    }

    private IAIState work() {
        if (!canStudy()) {
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
            if (state.recordScholarWork(world.getGameTime())) {
                data.markChanged();
            }
        }
        return AIWorkerState.START_WORKING;
    }

    private boolean canStudy() {
        return building != null && worker != null && worker.isAlive();
    }
}
