package com.imperium.realms.colony;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A persisted parliamentary bill reviewed by four modeled factions and the
 * Emperor. Faction votes are snapshotted when the bill is created, and the
 * final decision records who resolved it for a durable political audit trail.
 */
public final class ParliamentProposal {
    private static final int TOTAL_SEATS = 5;
    private static final int REQUIRED_MAJORITY = TOTAL_SEATS / 2 + 1;
    private static final String[] COUNCIL_SEATS = {"merchants", "commons", "nobility", "scholars"};

    public enum Type {
        TAX_RATE,
        ECONOMIC_POLICY
    }

    public enum Status {
        OPEN,
        PASSED,
        REJECTED,
        EXPIRED
    }

    private final long id;
    private final Type type;
    private final String proposer;
    private final int previousTaxRate;
    private final int proposedTaxRate;
    private final EconomicPolicy previousPolicy;
    private final EconomicPolicy proposedPolicy;
    private final long createdDay;
    private final long expiresDay;
    private final Map<String, Boolean> councilVotes;
    private Status status;
    private String resolvedBy;
    private long resolvedDay;

    private ParliamentProposal(
            final long id,
            final Type type,
            final String proposer,
            final int previousTaxRate,
            final int proposedTaxRate,
            final EconomicPolicy previousPolicy,
            final EconomicPolicy proposedPolicy,
            final long createdDay,
            final long expiresDay,
            final Map<String, Boolean> councilVotes,
            final Status status,
            final String resolvedBy,
            final long resolvedDay) {
        if (id < 1L) {
            throw new IllegalArgumentException("proposal id must be positive");
        }
        this.type = Objects.requireNonNull(type, "type");
        if (type == Type.TAX_RATE) {
            if (previousTaxRate < 0 || previousTaxRate > 25
                    || proposedTaxRate < 0 || proposedTaxRate > 25) {
                throw new IllegalArgumentException("tax rates must be between 0 and 25");
            }
        } else {
            Objects.requireNonNull(previousPolicy, "previousPolicy");
            Objects.requireNonNull(proposedPolicy, "proposedPolicy");
        }
        if (createdDay < 0L || expiresDay < createdDay) {
            throw new IllegalArgumentException("invalid proposal day range");
        }

        this.id = id;
        this.proposer = proposer == null || proposer.isBlank() ? "Unknown" : proposer.trim();
        this.previousTaxRate = previousTaxRate;
        this.proposedTaxRate = proposedTaxRate;
        this.previousPolicy = previousPolicy;
        this.proposedPolicy = proposedPolicy;
        this.createdDay = createdDay;
        this.expiresDay = expiresDay;
        this.councilVotes = new LinkedHashMap<>();
        if (councilVotes != null) {
            for (final String seat : COUNCIL_SEATS) {
                if (councilVotes.containsKey(seat)) {
                    this.councilVotes.put(seat, Boolean.TRUE.equals(councilVotes.get(seat)));
                }
            }
        }
        this.status = Objects.requireNonNullElse(status, Status.OPEN);
        this.resolvedBy = resolvedBy == null ? "" : resolvedBy.trim();
        this.resolvedDay = Math.max(-1L, resolvedDay);
    }

    static ParliamentProposal restore(
            final long id,
            final Type type,
            final String proposer,
            final int previousTaxRate,
            final int proposedTaxRate,
            final EconomicPolicy previousPolicy,
            final EconomicPolicy proposedPolicy,
            final long createdDay,
            final long expiresDay,
            final Map<String, Boolean> councilVotes,
            final Status status,
            final String resolvedBy,
            final long resolvedDay) {
        return new ParliamentProposal(id, type, proposer, previousTaxRate, proposedTaxRate,
                previousPolicy, proposedPolicy, createdDay, expiresDay, councilVotes, status,
                resolvedBy, resolvedDay);
    }

    static ParliamentProposal createTaxProposal(
            final long id,
            final String proposer,
            final int currentTaxRate,
            final int proposedTaxRate,
            final long currentDay,
            final EconomicPolicy policy,
            final int stability) {
        final long safeDay = Math.max(0L, currentDay);
        final long expiry = safeDay > Long.MAX_VALUE - 3L ? Long.MAX_VALUE : safeDay + 3L;
        final Map<String, Boolean> votes = taxVotes(currentTaxRate, proposedTaxRate, policy, stability);
        return new ParliamentProposal(id, Type.TAX_RATE, proposer,
                currentTaxRate, proposedTaxRate, policy, policy, safeDay, expiry,
                votes, Status.OPEN, "", -1L);
    }

    static ParliamentProposal createPolicyProposal(
            final long id,
            final String proposer,
            final int currentTaxRate,
            final EconomicPolicy currentPolicy,
            final EconomicPolicy proposedPolicy,
            final long currentDay,
            final int stability) {
        final long safeDay = Math.max(0L, currentDay);
        final long expiry = safeDay > Long.MAX_VALUE - 3L ? Long.MAX_VALUE : safeDay + 3L;
        final Map<String, Boolean> votes = policyVotes(proposedPolicy, stability);
        return new ParliamentProposal(id, Type.ECONOMIC_POLICY, proposer,
                currentTaxRate, currentTaxRate, currentPolicy, proposedPolicy,
                safeDay, expiry, votes, Status.OPEN, "", -1L);
    }

    private static Map<String, Boolean> taxVotes(
            final int currentTaxRate,
            final int proposedTaxRate,
            final EconomicPolicy policy,
            final int stability) {
        final Map<String, Boolean> votes = new LinkedHashMap<>();
        // Transparent first-pass political heuristics. Later simulation can
        // replace them with separately evolving faction approval scores.
        votes.put("merchants", proposedTaxRate <= currentTaxRate || proposedTaxRate <= 5);
        votes.put("commons", proposedTaxRate < currentTaxRate
                || (policy == EconomicPolicy.WELFARE
                    && stability >= 55
                    && proposedTaxRate <= currentTaxRate + 5));
        votes.put("nobility", proposedTaxRate <= currentTaxRate + 1);
        votes.put("scholars", proposedTaxRate <= currentTaxRate
                || (policy == EconomicPolicy.WELFARE && stability >= 60)
                || stability >= 80);
        return votes;
    }

    private static Map<String, Boolean> policyVotes(
            final EconomicPolicy policy,
            final int stability) {
        final Map<String, Boolean> votes = new LinkedHashMap<>();
        votes.put("merchants", policy == EconomicPolicy.MERCANTILE
                || policy == EconomicPolicy.BALANCED);
        votes.put("commons", policy == EconomicPolicy.WELFARE
                || (policy == EconomicPolicy.BALANCED && stability < 75));
        votes.put("nobility", policy == EconomicPolicy.MERCANTILE
                || policy == EconomicPolicy.AUSTERITY
                || (policy == EconomicPolicy.BALANCED && stability < 40));
        votes.put("scholars", policy == EconomicPolicy.WELFARE
                || policy == EconomicPolicy.BALANCED
                || (policy == EconomicPolicy.MERCANTILE && stability < 60));
        return votes;
    }

    public long id() {
        return id;
    }

    public Type type() {
        return type;
    }

    public String typeId() {
        return type.name().toLowerCase(Locale.ROOT);
    }

    public String proposer() {
        return proposer;
    }

    public int previousTaxRate() {
        return previousTaxRate;
    }

    public int proposedTaxRate() {
        return proposedTaxRate;
    }

    public EconomicPolicy previousPolicy() {
        return previousPolicy;
    }

    public EconomicPolicy proposedPolicy() {
        return proposedPolicy;
    }

    public long createdDay() {
        return createdDay;
    }

    public long expiresDay() {
        return expiresDay;
    }

    public Status status() {
        return status;
    }

    public String resolvedBy() {
        return resolvedBy;
    }

    public long resolvedDay() {
        return resolvedDay;
    }

    public boolean isOpen() {
        return status == Status.OPEN;
    }

    public int councilYesVotes() {
        return (int) councilVotes.values().stream().filter(Boolean::booleanValue).count();
    }

    public int councilNoVotes() {
        return councilVotes.size() - councilYesVotes();
    }

    public Map<String, Boolean> councilVotes() {
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(councilVotes));
    }

    public String valueId() {
        return type == Type.TAX_RATE ? proposedTaxRate + "%" : proposedPolicy.id();
    }

    /**
     * Applies the Emperor's assent or veto. A bill needs at least three of
     * five votes, including the Emperor, before its value can be applied.
     */
    Status resolveByEmperor(
            final boolean emperorAssents,
            final long currentDay,
            final int currentTaxRate,
            final EconomicPolicy currentPolicy,
            final String emperorName) {
        if (!isOpen()) {
            return status;
        }
        if (currentDay > expiresDay) {
            setResolution(Status.EXPIRED, "Deadline", currentDay);
            return status;
        }

        final boolean unchanged = type == Type.TAX_RATE
                ? proposedTaxRate == currentTaxRate
                : proposedPolicy == currentPolicy;
        if (!emperorAssents || unchanged
                || councilYesVotes() + 1 < REQUIRED_MAJORITY
                || councilVotes.size() < TOTAL_SEATS - 1) {
            setResolution(Status.REJECTED, emperorAssents ? "Parliament" : emperorName, currentDay);
            return status;
        }

        setResolution(Status.PASSED, emperorName, currentDay);
        return status;
    }

    boolean expireIfDue(final long currentDay) {
        if (isOpen() && currentDay > expiresDay) {
            setResolution(Status.EXPIRED, "Deadline", currentDay);
            return true;
        }
        return false;
    }

    private void setResolution(final Status newStatus, final String actor, final long day) {
        status = newStatus;
        resolvedBy = actor == null || actor.isBlank() ? "Unknown" : actor.trim();
        resolvedDay = Math.max(createdDay, day);
    }

    public String statusId() {
        return status.name().toLowerCase(Locale.ROOT);
    }
}
