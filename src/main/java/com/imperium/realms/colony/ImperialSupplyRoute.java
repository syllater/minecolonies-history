package com.imperium.realms.colony;

import java.util.Objects;

/**
 * Persistent logistics link from an imperial capital to one member province.
 * Routes are direct one-hop links in the initial logistics simulation.
 */
public final class ImperialSupplyRoute {
    public static final int MAX_CONDITION = 100;
    public static final int MIN_ACTIVE_CONDITION = 40;
    public static final int DAILY_REPAIR = 5;
    public static final int DAILY_WEAR_WITHOUT_UPKEEP = 10;

    private final ColonyIdentity destination;
    private final long builtDay;
    private int condition;
    private long lastUpkeepDay;

    private ImperialSupplyRoute(
            final ColonyIdentity destination,
            final long builtDay,
            final int condition,
            final long lastUpkeepDay) {
        this.destination = Objects.requireNonNull(destination, "destination");
        if (builtDay < 0L || lastUpkeepDay < builtDay) {
            throw new IllegalArgumentException("Invalid supply-route timing");
        }
        this.builtDay = builtDay;
        this.condition = clamp(condition);
        this.lastUpkeepDay = lastUpkeepDay;
    }

    static ImperialSupplyRoute build(final ColonyIdentity destination, final long dayIndex) {
        return new ImperialSupplyRoute(destination, dayIndex, MAX_CONDITION, dayIndex);
    }

    public static ImperialSupplyRoute restore(
            final ColonyIdentity destination,
            final long builtDay,
            final int condition,
            final long lastUpkeepDay) {
        return new ImperialSupplyRoute(destination, builtDay, condition, lastUpkeepDay);
    }

    public ColonyIdentity destination() {
        return destination;
    }

    public long builtDay() {
        return builtDay;
    }

    public int condition() {
        return condition;
    }

    public long lastUpkeepDay() {
        return lastUpkeepDay;
    }

    public boolean isActive() {
        return condition >= MIN_ACTIVE_CONDITION;
    }

    public boolean isDue(final long dayIndex) {
        return dayIndex > lastUpkeepDay;
    }

    /**
     * Processes one authoritative daily turn. Paid upkeep repairs the route;
     * unpaid upkeep wears it down. Skipped server time is not simulated in a burst.
     */
    boolean processDay(final long dayIndex, final boolean upkeepPaid) {
        if (dayIndex < 0L || !isDue(dayIndex)) {
            return false;
        }
        lastUpkeepDay = dayIndex;
        condition = upkeepPaid
                ? clamp(condition + DAILY_REPAIR)
                : clamp(condition - DAILY_WEAR_WITHOUT_UPKEEP);
        return true;
    }

    private static int clamp(final int value) {
        return Math.max(0, Math.min(MAX_CONDITION, value));
    }
}
