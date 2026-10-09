package com.imperium.realms.colony;

import com.imperium.realms.economy.ImperialPolicy;

import java.util.Objects;

/** Imperium-owned persistent state for one MineColonies colony. */
public final class EmpireState {
    private static final long TICKS_PER_DAY = 24_000L;

    private final ColonyIdentity identity;
    private String colonyName;
    private final long firstSeenGameTime;
    private long lastSeenGameTime;
    private ImperialPolicy policy;
    private long treasuryCrowns;
    private long diplomaticInfluence;
    private long lastEconomyDay;
    private long lastDiplomaticDay;

    EmpireState(
            final ColonyIdentity identity, final String colonyName,
            final long firstSeenGameTime, final long lastSeenGameTime,
            final ImperialPolicy policy, final long treasuryCrowns,
            final long diplomaticInfluence, final long lastEconomyDay,
            final long lastDiplomaticDay) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.colonyName = normalizeName(colonyName);
        this.firstSeenGameTime = Math.max(0L, firstSeenGameTime);
        this.lastSeenGameTime = Math.max(this.firstSeenGameTime, lastSeenGameTime);
        this.policy = Objects.requireNonNullElse(policy, ImperialPolicy.BALANCED);
        this.treasuryCrowns = Math.max(0L, treasuryCrowns);
        this.diplomaticInfluence = Math.max(0L, diplomaticInfluence);
        this.lastEconomyDay = Math.max(-1L, lastEconomyDay);
        this.lastDiplomaticDay = Math.max(-1L, lastDiplomaticDay);
    }

    public ColonyIdentity identity() { return identity; }
    public String colonyName() { return colonyName; }
    public long firstSeenGameTime() { return firstSeenGameTime; }
    public long lastSeenGameTime() { return lastSeenGameTime; }
    public ImperialPolicy policy() { return policy; }
    public long treasuryCrowns() { return treasuryCrowns; }
    public long diplomaticInfluence() { return diplomaticInfluence; }
    public long lastEconomyDay() { return lastEconomyDay; }
    public long lastDiplomaticDay() { return lastDiplomaticDay; }

    boolean observe(final String currentColonyName, final long gameTime) {
        boolean changed = false;
        final String name = normalizeName(currentColonyName);
        if (!colonyName.equals(name)) {
            colonyName = name;
            changed = true;
        }
        final long safeTime = Math.max(firstSeenGameTime, gameTime);
        if (gameDay(safeTime) > gameDay(lastSeenGameTime)) {
            lastSeenGameTime = safeTime;
            changed = true;
        }
        return changed;
    }

    static EmpireState create(final ColonyIdentity identity, final String name, final long gameTime) {
        final long safeTime = Math.max(0L, gameTime);
        return new EmpireState(identity, name, safeTime, safeTime,
                ImperialPolicy.BALANCED, 0L, 0L, gameDay(safeTime), -1L);
    }

    public boolean setPolicy(final ImperialPolicy nextPolicy) {
        Objects.requireNonNull(nextPolicy, "nextPolicy");
        if (policy == nextPolicy) return false;
        policy = nextPolicy;
        return true;
    }

    /** Apply taxes once after the overworld advances to a new day. */
    long collectDailyTax(final long gameTime, final int residentCount) {
        final long day = gameDay(gameTime);
        if (day <= lastEconomyDay) return 0L;
        lastEconomyDay = day;
        final long income = Math.max(0, residentCount) * (long) policy.crownsPerCitizenPerDay();
        if (income <= 0L) return 0L;
        final long previous = treasuryCrowns;
        treasuryCrowns = saturatedAdd(treasuryCrowns, income);
        return treasuryCrowns - previous;
    }

    /** A Diplomat generates at most one influence point per Minecraft day. */
    boolean recordDiplomaticWork(final long gameTime) {
        final long day = gameDay(gameTime);
        if (day <= lastDiplomaticDay) return false;
        lastDiplomaticDay = day;
        if (diplomaticInfluence < Long.MAX_VALUE) diplomaticInfluence++;
        return true;
    }

    public boolean canCreditCrowns(final long amount) {
        return amount > 0L && amount <= Long.MAX_VALUE - treasuryCrowns;
    }

    boolean creditCrowns(final long amount) {
        if (!canCreditCrowns(amount)) return false;
        treasuryCrowns += amount;
        return true;
    }

    boolean spendCrowns(final long amount) {
        if (amount <= 0L || amount > treasuryCrowns) return false;
        treasuryCrowns -= amount;
        return true;
    }

    static long gameDay(final long gameTime) {
        return Math.floorDiv(Math.max(0L, gameTime), TICKS_PER_DAY);
    }

    private static long saturatedAdd(final long current, final long amount) {
        return amount > Long.MAX_VALUE - current ? Long.MAX_VALUE : current + amount;
    }

    private static String normalizeName(final String name) {
        return name == null || name.isBlank() ? "Unnamed colony" : name.trim();
    }
}
