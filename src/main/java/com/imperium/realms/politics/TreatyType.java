package com.imperium.realms.politics;

import java.util.Arrays;
import java.util.Optional;

/**
 * Bilateral diplomatic agreements. A treaty is only applied after acceptance
 * by a member with colony management permission on the receiving side.
 */
public enum TreatyType {
    FRIENDSHIP("friendship", 20),
    TRADE_PACT("trade_pact", 30),
    NON_AGGRESSION("non_aggression", 45),
    ALLIANCE("alliance", 75);

    private final String id;
    private final int initialStanding;

    TreatyType(final String id, final int initialStanding) {
        this.id = id;
        this.initialStanding = initialStanding;
    }

    public String id() {
        return id;
    }

    public int initialStanding() {
        return initialStanding;
    }

    public String translationKey() {
        return "diplomacy.imperium.treaty." + id;
    }

    public static Optional<TreatyType> fromId(final String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values()).filter(treaty -> treaty.id.equalsIgnoreCase(id)).findFirst();
    }
}
