package com.imperium.realms.colony;

import com.imperium.realms.economy.EmpirePolicy;

import java.util.Objects;
import java.util.Optional;

/**
 * Imperium-owned state for one MineColonies colony.
 *
 * <p>The model stays separate from MineColonies' private colony NBT. It is
 * persisted by Imperium SavedData and is authoritative on the server.</p>
 */
public final class EmpireState {
    private final ColonyIdentity identity;
    private String colonyName;
    private final long firstSeenGameTime;
    private long lastSeenGameTime;

    private long treasury;
    private int taxRatePercent;
    private EmpirePolicy policy;
    private long lastTaxCollectionDay;

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime) {
        this(identity, colonyName, firstSeenGameTime, lastSeenGameTime,
                0L, 10, EmpirePolicy.BALANCED, -1L);
    }

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime,
            final long treasury,
            final int taxRatePercent,
            final EmpirePolicy policy,
            final long lastTaxCollectionDay) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.colonyName = normalizeName(colonyName);
        this.firstSeenGameTime = Math.max(0L, firstSeenGameTime);
        this.lastSeenGameTime = Math.max(this.firstSeenGameTime, lastSeenGameTime);
        this.treasury = Math.max(0L, treasury);
        this.taxRatePercent = clampTaxRate(taxRatePercent);
        this.policy = Objects.requireNonNullElse(policy, EmpirePolicy.BALANCED);
        this.lastTaxCollectionDay = lastTaxCollectionDay;
    }

    public ColonyIdentity identity() {
        return identity;
    }

    public String colonyName() {
        return colonyName;
    }

    public long firstSeenGameTime() {
        return firstSeenGameTime;
    }

    public long lastSeenGameTime() {
        return lastSeenGameTime;
    }

    /** Treasury balance in imperial crowns. */
    public long treasury() {
        return treasury;
    }

    /** Tax percentage expressed as 0 through 50. */
    public int taxRatePercent() {
        return taxRatePercent;
    }

    public EmpirePolicy policy() {
        return policy;
    }

    public long lastTaxCollectionDay() {
        return lastTaxCollectionDay;
    }

    /**
     * Refreshes only external observations. It never resets the first-seen time.
     *
     * @return true if a persisted field changed.
     */
    boolean observe(final String currentColonyName, final long gameTime) {
        boolean changed = false;
        final String normalizedName = normalizeName(currentColonyName);
        if (!colonyName.equals(normalizedName)) {
            colonyName = normalizedName;
            changed = true;
        }

        // A scan may happen every few seconds. Persist a heartbeat at most once
        // per in-game day to avoid marking SavedData dirty on every scan.
        final long safeGameTime = Math.max(firstSeenGameTime, gameTime);
        if (Math.floorDiv(safeGameTime, 24_000L) > Math.floorDiv(lastSeenGameTime, 24_000L)) {
            lastSeenGameTime = safeGameTime;
            changed = true;
        }
        return changed;
    }

    static EmpireState create(
            final ColonyIdentity identity,
            final String colonyName,
            final long gameTime) {
        final long safeTime = Math.max(0L, gameTime);
        return new EmpireState(identity, colonyName, safeTime, safeTime);
    }

    boolean setTaxRatePercent(final int percent) {
        if (percent < 0 || percent > 50) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 50 percent");
        }
        if (taxRatePercent == percent) {
            return false;
        }
        taxRatePercent = percent;
        return true;
    }

    boolean setPolicy(final EmpirePolicy newPolicy) {
        Objects.requireNonNull(newPolicy, "newPolicy");
        if (policy == newPolicy) {
            return false;
        }
        policy = newPolicy;
        return true;
    }

    /**
     * Collects taxes once per in-game day.
     *
     * <p>The gross revenue is the configured percentage expressed as crowns per
     * citizen (10% yields 10 crowns per citizen). Policy upkeep is paid from the
     * treasury after gross revenue is added. The balance cannot become negative.</p>
     *
     * @return an empty result if today's collection already happened.
     */
    Optional<TaxCollectionResult> collectTaxes(final int population, final long gameTime) {
        final long day = Math.floorDiv(Math.max(0L, gameTime), 24_000L);
        if (day <= lastTaxCollectionDay) {
            return Optional.empty();
        }

        final long safePopulation = Math.max(0, population);
        final long grossRevenue = safeMultiply(safePopulation, taxRatePercent);
        final long upkeepDue = safeMultiply(safePopulation, policy.dailyUpkeepPerCitizen());
        treasury = saturatingAdd(treasury, grossRevenue);
        final long upkeepPaid = Math.min(treasury, upkeepDue);
        treasury -= upkeepPaid;
        lastTaxCollectionDay = day;

        return Optional.of(new TaxCollectionResult(
                day, safePopulation, grossRevenue, upkeepDue, upkeepPaid, treasury));
    }

    private static long safeMultiply(final long left, final long right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    private static long saturatingAdd(final long left, final long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    static int clampTaxRate(final int percent) {
        return Math.max(0, Math.min(50, percent));
    }

    private static String normalizeName(final String name) {
        if (name == null || name.isBlank()) {
            return "Unnamed colony";
        }
        return name.trim();
    }

    public record TaxCollectionResult(
            long gameDay,
            long population,
            long grossRevenue,
            long upkeepDue,
            long upkeepPaid,
            long treasuryBalance) {
        public long netChange() {
            return grossRevenue - upkeepPaid;
        }

        public long unpaidUpkeep() {
            return upkeepDue - upkeepPaid;
        }
    }
}
