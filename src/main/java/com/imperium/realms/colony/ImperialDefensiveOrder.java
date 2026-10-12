package com.imperium.realms.colony;

import java.util.Objects;

/** Time-limited emergency defence order for one province of an imperial realm. */
public final class ImperialDefensiveOrder {
    public static final long COST_CROWNS = 50L;
    public static final long DURATION_DAYS = 7L;
    public static final int READINESS_BONUS = 20;

    private final ColonyIdentity province;
    private final String issuer;
    private final long issuedDay;
    private final long expiresDay;

    private ImperialDefensiveOrder(
            final ColonyIdentity province,
            final String issuer,
            final long issuedDay,
            final long expiresDay) {
        this.province = Objects.requireNonNull(province, "province");
        this.issuer = issuer == null || issuer.isBlank() ? "Unknown" : issuer.trim();
        if (issuedDay < 0L || expiresDay < issuedDay) {
            throw new IllegalArgumentException("Invalid defensive order timing");
        }
        this.issuedDay = issuedDay;
        this.expiresDay = expiresDay;
    }

    static ImperialDefensiveOrder issue(
            final ColonyIdentity province, final String issuer, final long issuedDay) {
        final long expiresDay = issuedDay > Long.MAX_VALUE - DURATION_DAYS
                ? Long.MAX_VALUE : issuedDay + DURATION_DAYS;
        return new ImperialDefensiveOrder(province, issuer, issuedDay, expiresDay);
    }

    public static ImperialDefensiveOrder restore(
            final ColonyIdentity province, final String issuer,
            final long issuedDay, final long expiresDay) {
        return new ImperialDefensiveOrder(province, issuer, issuedDay, expiresDay);
    }

    public ColonyIdentity province() { return province; }
    public String issuer() { return issuer; }
    public long issuedDay() { return issuedDay; }
    public long expiresDay() { return expiresDay; }

    public boolean isActive(final long currentDay) {
        return currentDay >= issuedDay && currentDay < expiresDay;
    }
}
