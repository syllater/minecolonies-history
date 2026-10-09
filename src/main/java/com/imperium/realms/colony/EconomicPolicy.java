package com.imperium.realms.colony;

import java.util.Arrays;
import java.util.Optional;

/**
 * Empire-level tax/spending trade-offs. Effects are applied only by the
 * server-side daily economy tick.
 */
public enum EconomicPolicy {
    BALANCED("balanced", 100, 0),
    MERCANTILE("mercantile", 125, 0),
    WELFARE("welfare", 75, 2),
    AUSTERITY("austerity", 150, -2);

    private final String id;
    private final int taxMultiplierPercent;
    private final int dailyStabilityChange;

    EconomicPolicy(String id, int taxMultiplierPercent, int dailyStabilityChange) {
        this.id = id;
        this.taxMultiplierPercent = taxMultiplierPercent;
        this.dailyStabilityChange = dailyStabilityChange;
    }

    public String id() {
        return id;
    }

    int taxMultiplierPercent() {
        return taxMultiplierPercent;
    }

    int dailyStabilityChange() {
        return dailyStabilityChange;
    }

    public static Optional<EconomicPolicy> fromId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(policy -> policy.id.equalsIgnoreCase(id.trim()))
                .findFirst();
    }
}
