package com.imperium.realms.colony;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ImperialStrategyPlannerTest {
    private final ColonyIdentity capital = new ColonyIdentity("minecraft:overworld", 1);
    private final ColonyIdentity provinceA = new ColonyIdentity("minecraft:overworld", 2);
    private final ColonyIdentity provinceB = new ColonyIdentity("minecraft:overworld", 3);

    @Test
    void routePlannerSkipsCapitalAndAlreadyRoutedProvinces() {
        final EmpireRealm realm = realmWithTwoProvinces();
        assertTrue(realm.depositImperialTreasury(EmpireRealm.SUPPLY_ROUTE_BUILD_COST));
        assertTrue(realm.buildSupplyRoute(provinceA, 1L, "Emperor"));

        final Map<ColonyIdentity, EmpireState> states = new LinkedHashMap<>();
        states.put(capital, EmpireState.create(capital, "Capital", 0L));
        states.put(provinceA, EmpireState.create(provinceA, "West", 0L));
        states.put(provinceB, EmpireState.create(provinceB, "East", 0L));

        final Optional<ColonyIdentity> target = ImperialStrategyPlanner.nextSupplyRouteTarget(
                realm, identity -> Optional.ofNullable(states.get(identity)));

        assertEquals(Optional.of(provinceB), target);
    }

    @Test
    void routePlannerPrioritizesTheProvinceWithGreatestInstability() {
        final EmpireRealm realm = realmWithTwoProvinces();
        final Map<ColonyIdentity, EmpireState> states = new LinkedHashMap<>();
        final EmpireState capitalState = EmpireState.create(capital, "Capital", 0L);
        final EmpireState stableState = EmpireState.create(provinceA, "Stable", 0L);
        final EmpireState unstableState = EmpireState.create(provinceB, "Unrest", 0L);
        unstableState.restorePoliticalSimulation(
                Map.of("merchants", 20, "commons", 20, "nobility", 20, "scholars", 20),
                80, EmpireState.CivicDisorder.STRIKE, 1L);
        states.put(capital, capitalState);
        states.put(provinceA, stableState);
        states.put(provinceB, unstableState);

        assertEquals(Optional.of(provinceB), ImperialStrategyPlanner.nextSupplyRouteTarget(
                realm, identity -> Optional.ofNullable(states.get(identity))));
    }

    @Test
    void defensePlannerSelectsNextProvinceAfterMostVulnerableHasAnActiveOrder() {
        final EmpireRealm realm = realmWithTwoProvinces();
        assertTrue(realm.depositImperialTreasury(ImperialDefensiveOrder.COST_CROWNS));
        assertTrue(realm.issueDefensiveOrder(provinceB, "Emperor", 4L));

        final Map<ColonyIdentity, EmpireState> states = new LinkedHashMap<>();
        states.put(capital, EmpireState.create(capital, "Capital", 0L));
        states.put(provinceA, EmpireState.create(provinceA, "West", 0L));
        final EmpireState unstableState = EmpireState.create(provinceB, "East", 0L);
        unstableState.restorePoliticalSimulation(
                Map.of("merchants", 10, "commons", 10, "nobility", 10, "scholars", 10),
                90, EmpireState.CivicDisorder.REVOLT, 1L);
        states.put(provinceB, unstableState);

        assertEquals(Optional.of(provinceA), ImperialStrategyPlanner.mostAtRiskProvince(
                realm, identity -> Optional.ofNullable(states.get(identity)), 5L));
    }

    @Test
    void plannerReturnsEmptyWhenNoEligibleTargetExists() {
        final EmpireRealm realm = EmpireRealm.found(
                4L, "Single Province", capital, "emperor", "Emperor", 0L);
        final Map<ColonyIdentity, EmpireState> states = Map.of(
                capital, EmpireState.create(capital, "Capital", 0L));

        assertTrue(ImperialStrategyPlanner.nextSupplyRouteTarget(
                realm, identity -> Optional.ofNullable(states.get(identity))).isEmpty());
        assertEquals(Optional.of(capital), ImperialStrategyPlanner.mostAtRiskProvince(
                realm, identity -> Optional.ofNullable(states.get(identity)), 0L));
        assertTrue(ImperialStrategyPlanner.mostAtRiskProvince(
                realm, identity -> Optional.ofNullable(states.get(identity)), -1L).isEmpty());
    }

    private EmpireRealm realmWithTwoProvinces() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "emperor", "Emperor", 0L);
        assertTrue(realm.inviteProvince(provinceA, 0L));
        assertTrue(realm.acceptInvitation(provinceA, 0L));
        assertTrue(realm.inviteProvince(provinceB, 0L));
        assertTrue(realm.acceptInvitation(provinceB, 0L));
        return realm;
    }
}
