package com.imperium.realms.government;

import java.util.Arrays;
import java.util.Optional;

/** Government constitutions supported by the empire simulation. */
public enum GovernmentType {
    CONSTITUTIONAL_MONARCHY("constitutional_monarchy"),
    ABSOLUTE_MONARCHY("absolute_monarchy"),
    MERCHANT_REPUBLIC("merchant_republic");

    private final String id;

    GovernmentType(final String id) { this.id = id; }

    public String id() { return id; }
    public String translationKey() { return "imperium.government." + id; }

    public static Optional<GovernmentType> fromId(final String id) {
        if (id == null) return Optional.empty();
        return Arrays.stream(values()).filter(type -> type.id.equalsIgnoreCase(id)).findFirst();
    }
}
