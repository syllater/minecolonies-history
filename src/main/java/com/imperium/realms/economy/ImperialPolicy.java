package com.imperium.realms.economy;

import java.util.Arrays;
import java.util.Optional;

/** Per-colony tax policy. Rates are crowns per resident per Minecraft day. */
public enum ImperialPolicy {
    LOW_TAX("low_tax", 1),
    BALANCED("balanced", 2),
    EMERGENCY_LEVY("emergency_levy", 4);

    private final String id;
    private final int crownsPerCitizenPerDay;

    ImperialPolicy(final String id, final int crownsPerCitizenPerDay) {
        this.id = id;
        this.crownsPerCitizenPerDay = crownsPerCitizenPerDay;
    }

    public String id() { return id; }
    public int crownsPerCitizenPerDay() { return crownsPerCitizenPerDay; }
    public String translationKey() { return "imperium.policy." + id; }

    public static Optional<ImperialPolicy> fromId(final String id) {
        if (id == null) return Optional.empty();
        return Arrays.stream(values()).filter(policy -> policy.id.equalsIgnoreCase(id)).findFirst();
    }
}
