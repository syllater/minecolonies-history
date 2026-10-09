package com.imperium.realms.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Imperium-owned state for one MineColonies colony. All mutable state in this
 * object is persisted by EmpireStateSavedData, never in MineColonies-private NBT.
 */
public final class EmpireState {
    public static final long MAX_TREASURY = 1_000_000_000L;
    private static final int MIN_TAX_RATE = 0;
    private static final int MAX_TAX_RATE = 25;
    private static final long SCHOLAR_WORK_INTERVAL_TICKS = 1_200L;
    private static final int MAX_STORED_PROPOSALS = 20;

    public enum TaxProposalResolution {
        PASSED,
        REJECTED,
        EXPIRED,
        NOT_FOUND,
        ALREADY_RESOLVED
    }

    private final ColonyIdentity identity;
    private String colonyName;
    private final long firstSeenGameTime;
    private long lastSeenGameTime;

    private long treasuryCrowns;
    private int taxRatePercent = 5;
    private EconomicPolicy economicPolicy = EconomicPolicy.BALANCED;
    private long knowledgePoints;
    private int stability = 50;
    private long lastTaxDay = -1L;
    private long lastScholarWorkTick = -1L;

    private long nextProposalId = 1L;
    private final List<ParliamentProposal> parliamentProposals = new ArrayList<>();

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime) {
        this(identity, colonyName, firstSeenGameTime, lastSeenGameTime,
                0L, 5, EconomicPolicy.BALANCED, 0L, 50, -1L, -1L);
    }

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime,
            final long treasuryCrowns,
            final int taxRatePercent,
            final EconomicPolicy economicPolicy,
            final long knowledgePoints,
            final int stability,
            final long lastTaxDay,
            final long lastScholarWorkTick) {
        this(identity, colonyName, firstSeenGameTime, lastSeenGameTime, treasuryCrowns,
                taxRatePercent, economicPolicy, knowledgePoints, stability, lastTaxDay,
                lastScholarWorkTick, 1L, List.of());
    }

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime,
            final long treasuryCrowns,
            final int taxRatePercent,
            final EconomicPolicy economicPolicy,
            final long knowledgePoints,
            final int stability,
            final long lastTaxDay,
            final long lastScholarWorkTick,
            final long nextProposalId,
            final List<ParliamentProposal> loadedProposals) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.colonyName = normalizeName(colonyName);
        this.firstSeenGameTime = Math.max(0L, firstSeenGameTime);
        this.lastSeenGameTime = Math.max(this.firstSeenGameTime, lastSeenGameTime);
        this.treasuryCrowns = clamp(treasuryCrowns, 0L, MAX_TREASURY);
        this.taxRatePercent = clamp(taxRatePercent, MIN_TAX_RATE, MAX_TAX_RATE);
        this.economicPolicy = Objects.requireNonNullElse(economicPolicy, EconomicPolicy.BALANCED);
        this.knowledgePoints = Math.max(0L, knowledgePoints);
        this.stability = clamp(stability, 0, 100);
        this.lastTaxDay = lastTaxDay;
        this.lastScholarWorkTick = lastScholarWorkTick;
        this.nextProposalId = Math.max(1L, nextProposalId);

        if (loadedProposals != null) {
            for (final ParliamentProposal proposal : loadedProposals) {
                if (proposal != null) {
                    parliamentProposals.add(proposal);
                    if (proposal.id() < Long.MAX_VALUE && proposal.id() >= this.nextProposalId) {
                        this.nextProposalId = proposal.id() + 1L;
                    }
                }
            }
        }
        trimProposals();
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

    public long treasuryCrowns() {
        return treasuryCrowns;
    }

    public int taxRatePercent() {
        return taxRatePercent;
    }

    public EconomicPolicy economicPolicy() {
        return economicPolicy;
    }

    public long knowledgePoints() {
        return knowledgePoints;
    }

    public int stability() {
        return stability;
    }

    public long lastTaxDay() {
        return lastTaxDay;
    }

    public long lastScholarWorkTick() {
        return lastScholarWorkTick;
    }

    public long nextProposalId() {
        return nextProposalId;
    }

    /**
     * Refreshes observed metadata only. It never resets the first-seen time.
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

    public boolean setTaxRatePercent(final int newRate) {
        if (newRate < MIN_TAX_RATE || newRate > MAX_TAX_RATE) {
            return false;
        }
        if (taxRatePercent == newRate) {
            return false;
        }
        taxRatePercent = newRate;
        return true;
    }

    public boolean setEconomicPolicy(final EconomicPolicy newPolicy) {
        Objects.requireNonNull(newPolicy, "newPolicy");
        if (economicPolicy == newPolicy) {
            return false;
        }
        economicPolicy = newPolicy;
        return true;
    }

    public boolean creditTreasury(final long amount) {
        if (amount <= 0L || amount > MAX_TREASURY - treasuryCrowns) {
            return false;
        }
        treasuryCrowns += amount;
        return true;
    }

    public boolean debitTreasury(final long amount) {
        if (amount <= 0L || amount > treasuryCrowns) {
            return false;
        }
        treasuryCrowns -= amount;
        return true;
    }

    /** Invest crowns at a deterministic rate of 10 crowns per knowledge point. */
    public boolean investInKnowledge(final long amount) {
        if (amount < 10L || amount % 10L != 0L || amount > treasuryCrowns) {
            return false;
        }
        treasuryCrowns -= amount;
        knowledgePoints = Math.min(Long.MAX_VALUE - (amount / 10L), knowledgePoints) + (amount / 10L);
        return true;
    }

    /**
     * A scholar assigned to the Imperial Archive produces one knowledge point
     * no more often than every in-game minute.
     */
    public boolean recordScholarWork(final long gameTime) {
        final long safeTime = Math.max(0L, gameTime);
        if (lastScholarWorkTick >= 0L
                && (safeTime < lastScholarWorkTick
                    || safeTime - lastScholarWorkTick < SCHOLAR_WORK_INTERVAL_TICKS)) {
            return false;
        }
        if (knowledgePoints < Long.MAX_VALUE) {
            knowledgePoints++;
        }
        lastScholarWorkTick = safeTime;
        return true;
    }

    /**
     * Processes one day of tax. The return value is the amount collected this turn.
     */
    public long collectDailyTaxes(final long dayIndex, final long population) {
        expireParliamentProposals(dayIndex);
        if (dayIndex <= lastTaxDay) {
            return 0L;
        }

        lastTaxDay = dayIndex;
        stability = clamp(stability + economicPolicy.dailyStabilityChange(), 0, 100);

        final long safePopulation = Math.max(0L, Math.min(population, 1_000_000L));
        final long taxableBase = (safePopulation * taxRatePercent) / 5L;
        final long policyAdjusted = (taxableBase * economicPolicy.taxMultiplierPercent()) / 100L;
        final long deposited = Math.max(0L, Math.min(policyAdjusted, MAX_TREASURY - treasuryCrowns));
        treasuryCrowns += deposited;
        return deposited;
    }

    /**
     * Creates a tax bill for parliamentary review. The four faction votes are
     * calculated once at proposal time; the Emperor has a separate assent/veto.
     */
    public Optional<ParliamentProposal> createTaxProposal(
            final String proposer,
            final int proposedTaxRate,
            final long currentDay) {
        if (proposedTaxRate < MIN_TAX_RATE || proposedTaxRate > MAX_TAX_RATE
                || proposedTaxRate == taxRatePercent
                || nextProposalId <= 0L
                || nextProposalId == Long.MAX_VALUE) {
            return Optional.empty();
        }

        for (final ParliamentProposal existing : parliamentProposals) {
            if (existing.isOpen() && existing.proposedTaxRate() == proposedTaxRate) {
                return Optional.empty();
            }
        }

        final ParliamentProposal proposal = ParliamentProposal.createTaxProposal(
                nextProposalId++,
                proposer,
                taxRatePercent,
                proposedTaxRate,
                currentDay,
                economicPolicy,
                stability);
        parliamentProposals.add(proposal);
        trimProposals();
        return Optional.of(proposal);
    }

    public Optional<ParliamentProposal> findProposal(final long proposalId) {
        return parliamentProposals.stream()
                .filter(proposal -> proposal.id() == proposalId)
                .findFirst();
    }

    /** Most recent proposals first, returned as a detached snapshot. */
    public List<ParliamentProposal> recentParliamentProposals() {
        final List<ParliamentProposal> recent = new ArrayList<>(parliamentProposals);
        Collections.reverse(recent);
        return Collections.unmodifiableList(recent);
    }

    public boolean expireParliamentProposals(final long currentDay) {
        boolean changed = false;
        for (final ParliamentProposal proposal : parliamentProposals) {
            changed |= proposal.expireIfDue(currentDay);
        }
        return changed;
    }

    public TaxProposalResolution resolveTaxProposal(
            final long proposalId,
            final boolean emperorAssents,
            final long currentDay) {
        final ParliamentProposal proposal = findProposal(proposalId).orElse(null);
        if (proposal == null) {
            return TaxProposalResolution.NOT_FOUND;
        }
        if (!proposal.isOpen()) {
            return TaxProposalResolution.ALREADY_RESOLVED;
        }

        final ParliamentProposal.Status status =
                proposal.resolveByEmperor(emperorAssents, currentDay, taxRatePercent);
        if (status == ParliamentProposal.Status.EXPIRED) {
            return TaxProposalResolution.EXPIRED;
        }
        if (status != ParliamentProposal.Status.PASSED) {
            return TaxProposalResolution.REJECTED;
        }
        if (!setTaxRatePercent(proposal.proposedTaxRate())) {
            // Defensive guard: a parallel or stale bill must never rewrite state.
            return TaxProposalResolution.REJECTED;
        }
        return TaxProposalResolution.PASSED;
    }

    private void trimProposals() {
        while (parliamentProposals.size() > MAX_STORED_PROPOSALS) {
            parliamentProposals.remove(0);
        }
    }

    private static String normalizeName(final String name) {
        if (name == null || name.isBlank()) {
            return "Unnamed colony";
        }
        return name.trim();
    }

    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long clamp(final long value, final long min, final long max) {
        return Math.max(min, Math.min(max, value));
    }
}
