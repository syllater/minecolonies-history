package com.imperium.realms.colony;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Deterministic planner for quick strategic actions. It only recommends targets;
 * the server command handler still checks emperor permissions, treasury and realm
 * membership immediately before carrying out an action.
 */
public final class ImperialStrategyPlanner {
    private ImperialStrategyPlanner() {
    }

    /**
     * Select the most vulnerable member province that does not yet have a route.
     * The imperial capital is the route origin and is never selected as a target.
     */
    public static Optional<ColonyIdentity> nextSupplyRouteTarget(
            final EmpireRealm realm,
            final Function<ColonyIdentity, Optional<EmpireState>> stateLookup) {
        Objects.requireNonNull(realm, "realm");
        Objects.requireNonNull(stateLookup, "stateLookup");

        return realm.provinces().stream()
                .filter(identity -> !realm.capital().equals(identity))
                .filter(identity -> realm.supplyRouteTo(identity).isEmpty())
                .filter(identity -> stateLookup.apply(identity).isPresent())
                .sorted(priorityOrder(realm, stateLookup))
                .findFirst();
    }

    /**
     * Select the most vulnerable province without an active temporary defence
     * order. This can also select the capital because it is a valid defence target.
     */
    public static Optional<ColonyIdentity> mostAtRiskProvince(
            final EmpireRealm realm,
            final Function<ColonyIdentity, Optional<EmpireState>> stateLookup,
            final long currentDay) {
        Objects.requireNonNull(realm, "realm");
        Objects.requireNonNull(stateLookup, "stateLookup");
        if (currentDay < 0L) {
            return Optional.empty();
        }

        return realm.provinces().stream()
                .filter(identity -> realm.defensiveOrderBonus(identity, currentDay) == 0)
                .filter(identity -> stateLookup.apply(identity).isPresent())
                .sorted(priorityOrder(realm, stateLookup))
                .findFirst();
    }

    private static Comparator<ColonyIdentity> priorityOrder(
            final EmpireRealm realm,
            final Function<ColonyIdentity, Optional<EmpireState>> stateLookup) {
        return Comparator
                .comparingInt((ColonyIdentity identity) -> stateLookup.apply(identity)
                        .map(state -> riskScore(realm, identity, state))
                        .orElse(Integer.MIN_VALUE))
                .reversed()
                // Stable tie-breaker keeps automated orders reproducible after reloads.
                .thenComparing(ColonyIdentity::storageKey);
    }

    /**
     * Higher scores mean a stronger reason to receive scarce logistics/defence.
     * Stability and unrest are deliberately weighted above loyalty, so an
     * unstable colony is prioritised even if it has not yet petitioned to leave.
     */
    private static int riskScore(
            final EmpireRealm realm,
            final ColonyIdentity identity,
            final EmpireState state) {
        int score = (100 - state.stability()) * 2
                + state.unrest() * 2
                + (100 - realm.provincialLoyalty(identity));
        score += switch (state.civicDisorder()) {
            case CALM -> 0;
            case STRIKE -> 20;
            case REVOLT -> 45;
        };
        return score;
    }
}
