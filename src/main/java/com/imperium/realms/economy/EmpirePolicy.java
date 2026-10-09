package com.imperium.realms.economy;

import java.util.Arrays;
import java.util.Optional;

/**
 * Broad administrative priorities that affect daily treasury upkeep.
 */
public enum EmpirePolicy {
    BALANCED("balanced", 0),
    PUBLIC_WORKS("public_works", 2),
    SCHOLARSHIP("scholarship", 3);

    private final String id;
    private final int dailyUpkeepPerCitizen;

    EmpirePolicy(final String id, final int dailyUpkeepPerCitizen) {
        this.id = id;
        this.dailyUpkeepPerCitizen = dailyUpkeepPerCitizen;
    }

    public String id() {
        return id;
    }

    public int dailyUpkeepPerCitizen() {
        return dailyUpkeepPerCitizen;
    }

    public static Optional<EmpirePolicy> fromId(final String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(policy -> policy.id.equalsIgnoreCase(id))
                .findFirst();
    }
}
