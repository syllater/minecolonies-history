package com.imperium.realms.colony;

import com.imperium.realms.economy.EmpirePolicy;
import com.imperium.realms.politics.FactionType;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
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

    private int citizenApproval;
    private int unrest;
    private long lastPoliticsDay;
    private final EnumMap<FactionType, Integer> factionSupport = new EnumMap<>(FactionType.class);

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
        this(identity, colonyName, firstSeenGameTime, lastSeenGameTime, treasury, taxRatePercent,
                policy, lastTaxCollectionDay, 50, 0, 0L, defaultFactionSupport());
    }

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime,
            final long treasury,
            final int taxRatePercent,
            final EmpirePolicy policy,
            final long lastTaxCollectionDay,
            final int citizenApproval,
            final int unrest,
            final long lastPoliticsDay,
            final Map<FactionType, Integer> savedFactionSupport) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.colonyName = normalizeName(colonyName);
        this.firstSeenGameTime = Math.max(0L, firstSeenGameTime);
        this.lastSeenGameTime = Math.max(this.firstSeenGameTime, lastSeenGameTime);
        this.treasury = Math.max(0L, treasury);
        this.taxRatePercent = clampTaxRate(taxRatePercent);
        this.policy = Objects.requireNonNullElse(policy, EmpirePolicy.BALANCED);
        this.lastTaxCollectionDay = lastTaxCollectionDay;
        this.citizenApproval = clampPercent(citizenApproval);
        this.unrest = clampPercent(unrest);
        this.lastPoliticsDay = Math.max(0L, lastPoliticsDay);
        this.factionSupport.putAll(normalizeFactionSupport(savedFactionSupport));
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

    public int citizenApproval() {
        return citizenApproval;
    }

    public int unrest() {
        return unrest;
    }

    public long lastPoliticsDay() {
        return lastPoliticsDay;
    }

    public int factionSupport(final FactionType faction) {
        return factionSupport.getOrDefault(Objects.requireNonNull(faction, "faction"), 25);
    }

    public Map<FactionType, Integer> factionSupportSnapshot() {
        return Map.copyOf(factionSupport);
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
        return collectTaxes(population, 0, gameTime);
    }

    /**
     * Collect taxes once per in-game day. Each staffed Tax Collector adds five
     * percentage points to effective collection efficiency, up to a 75% total
     * cap. The configured tax rate itself remains bounded at 50%.
     */
    Optional<TaxCollectionResult> collectTaxes(
            final int population,
            final int taxCollectorCount,
            final long gameTime) {
        final long day = Math.floorDiv(Math.max(0L, gameTime), 24_000L);
        if (day <= lastTaxCollectionDay) {
            return Optional.empty();
        }

        final long safePopulation = Math.max(0, population);
        final long staffingBonus = Math.min(25L, Math.max(0L, (long) taxCollectorCount * 5L));
        final long effectiveTaxRate = Math.min(75L, (long) taxRatePercent + staffingBonus);
        final long grossRevenue = safeMultiply(safePopulation, effectiveTaxRate);
        final long upkeepDue = safeMultiply(safePopulation, policy.dailyUpkeepPerCitizen());
        treasury = saturatingAdd(treasury, grossRevenue);
        final long upkeepPaid = Math.min(treasury, upkeepDue);
        treasury -= upkeepPaid;
        lastTaxCollectionDay = day;

        return Optional.of(new TaxCollectionResult(
                day, safePopulation, grossRevenue, upkeepDue, upkeepPaid, treasury));
    }

    /**
     * Apply a once-per-day domestic politics update using MineColonies' actual
     * colony-wide happiness value, which is on a 0..10 scale in this release.
     */
    Optional<PoliticalReport> processPoliticsDay(final double overallHappiness, final long gameDay) {
        final long day = Math.max(0L, gameDay);
        if (day <= lastPoliticsDay) {
            return Optional.empty();
        }

        final int happinessPercent = Double.isFinite(overallHappiness)
                ? clampPercent((int) Math.round(overallHappiness * 10.0))
                : 55;
        final int previousApproval = citizenApproval;
        final int previousUnrest = unrest;

        int approvalChange = clamp((int) Math.round((happinessPercent - 55) / 12.0), -4, 4);
        if (taxRatePercent >= 30) {
            approvalChange -= 2;
        } else if (taxRatePercent >= 20) {
            approvalChange -= 1;
        } else if (taxRatePercent <= 10) {
            approvalChange += 1;
        }
        if (policy == EmpirePolicy.PUBLIC_WORKS) {
            approvalChange += 1;
        }
        citizenApproval = clampPercent(citizenApproval + approvalChange);

        int unrestChange;
        if (citizenApproval < 25) {
            unrestChange = 5;
        } else if (citizenApproval < 40) {
            unrestChange = 3;
        } else if (citizenApproval < 55) {
            unrestChange = 1;
        } else if (citizenApproval < 70) {
            unrestChange = -1;
        } else {
            unrestChange = -3;
        }
        if (happinessPercent >= 70) {
            unrestChange -= 2;
        } else if (happinessPercent <= 40) {
            unrestChange += 2;
        }
        if (policy == EmpirePolicy.PUBLIC_WORKS) {
            unrestChange -= 1;
        }
        if (taxRatePercent >= 25) {
            unrestChange += 1;
        }
        unrest = clampPercent(unrest + unrestChange);

        // Small, persistent transfers among domestic factions. The shares sum to 100
        // so future faction actions can use a predictable coalition model.
        if (policy == EmpirePolicy.PUBLIC_WORKS) {
            shiftFactionSupport(FactionType.CROWN_LOYALISTS, FactionType.COMMONERS_ASSEMBLY, 1);
        } else if (policy == EmpirePolicy.SCHOLARSHIP) {
            shiftFactionSupport(FactionType.COMMONERS_ASSEMBLY, FactionType.SCHOLARS_CIRCLE, 1);
        }

        if (taxRatePercent >= 25) {
            shiftFactionSupport(FactionType.MERCHANTS_GUILD, FactionType.COMMONERS_ASSEMBLY, 1);
        } else if (taxRatePercent <= 10) {
            shiftFactionSupport(FactionType.COMMONERS_ASSEMBLY, FactionType.MERCHANTS_GUILD, 1);
        }

        if (happinessPercent >= 70) {
            shiftFactionSupport(FactionType.COMMONERS_ASSEMBLY, FactionType.CROWN_LOYALISTS, 1);
        } else if (happinessPercent <= 40) {
            shiftFactionSupport(FactionType.CROWN_LOYALISTS, FactionType.COMMONERS_ASSEMBLY, 1);
        }

        lastPoliticsDay = day;
        return Optional.of(new PoliticalReport(
                day,
                happinessPercent,
                previousApproval,
                citizenApproval,
                previousUnrest,
                unrest,
                factionSupportSnapshot()));
    }

    private void shiftFactionSupport(
            final FactionType from,
            final FactionType to,
            final int requested) {
        final int available = factionSupport.getOrDefault(from, 25);
        final int room = 100 - factionSupport.getOrDefault(to, 25);
        final int amount = Math.max(0, Math.min(requested, Math.min(available, room)));
        if (amount == 0) {
            return;
        }
        factionSupport.put(from, available - amount);
        factionSupport.put(to, factionSupport.getOrDefault(to, 25) + amount);
    }

    private static Map<FactionType, Integer> defaultFactionSupport() {
        final EnumMap<FactionType, Integer> defaults = new EnumMap<>(FactionType.class);
        for (final FactionType faction : FactionType.values()) {
            defaults.put(faction, 25);
        }
        return defaults;
    }

    private static Map<FactionType, Integer> normalizeFactionSupport(final Map<FactionType, Integer> values) {
        if (values == null || values.isEmpty()) {
            return defaultFactionSupport();
        }

        final EnumMap<FactionType, Integer> source = new EnumMap<>(FactionType.class);
        int total = 0;
        for (final FactionType faction : FactionType.values()) {
            final int value = clampPercent(values.getOrDefault(faction, 25));
            source.put(faction, value);
            total += value;
        }
        if (total <= 0) {
            return defaultFactionSupport();
        }

        final EnumMap<FactionType, Integer> normalized = new EnumMap<>(FactionType.class);
        final EnumMap<FactionType, Double> remainders = new EnumMap<>(FactionType.class);
        int allocated = 0;
        for (final FactionType faction : FactionType.values()) {
            final double exact = source.get(faction) * 100.0 / total;
            final int base = (int) Math.floor(exact);
            normalized.put(faction, base);
            remainders.put(faction, exact - base);
            allocated += base;
        }

        while (allocated < 100) {
            FactionType best = FactionType.CROWN_LOYALISTS;
            double bestRemainder = -1.0;
            for (final FactionType faction : FactionType.values()) {
                if (remainders.get(faction) > bestRemainder) {
                    best = faction;
                    bestRemainder = remainders.get(faction);
                }
            }
            normalized.put(best, normalized.get(best) + 1);
            remainders.put(best, -1.0);
            allocated++;
        }
        return normalized;
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

    private static int clampPercent(final int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
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

    public record PoliticalReport(
            long gameDay,
            int happinessPercent,
            int previousApproval,
            int approval,
            int previousUnrest,
            int unrest,
            Map<FactionType, Integer> factionSupport) {
        public PoliticalReport {
            factionSupport = Map.copyOf(factionSupport);
        }
    }
}
