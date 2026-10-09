package com.imperium.realms.colony;


import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A persisted tax proposal reviewed by four modeled parliamentary factions and
 * the Emperor. Faction opinions are snapshotted at proposal time so a save/reload
 * or later policy change cannot silently rewrite a pending bill's votes.
 */
public final class ParliamentProposal {
    private static final int TOTAL_SEATS = 5;
    private static final int REQUIRED_MAJORITY = TOTAL_SEATS / 2 + 1;
    private static final String TAG_VOTES = "council_votes";

    public enum Status {
        OPEN,
        PASSED,
        REJECTED,
        EXPIRED
    }

    private final long id;
    private final String proposer;
    private final int previousTaxRate;
    private final int proposedTaxRate;
    private final long createdDay;
    private final long expiresDay;
    private final Map<String, Boolean> councilVotes;
    private Status status;

    private ParliamentProposal(
            final long id,
            final String proposer,
            final int previousTaxRate,
            final int proposedTaxRate,
            final long createdDay,
            final long expiresDay,
            final Map<String, Boolean> councilVotes,
            final Status status) {
        if (id < 1L) {
            throw new IllegalArgumentException("proposal id must be positive");
        }
        if (previousTaxRate < 0 || previousTaxRate > 25
                || proposedTaxRate < 0 || proposedTaxRate > 25) {
            throw new IllegalArgumentException("tax rates must be between 0 and 25");
        }
        if (createdDay < 0L || expiresDay < createdDay) {
            throw new IllegalArgumentException("invalid proposal day range");
        }

        this.id = id;
        this.proposer = proposer == null || proposer.isBlank() ? "Unknown" : proposer.trim();
        this.previousTaxRate = previousTaxRate;
        this.proposedTaxRate = proposedTaxRate;
        this.createdDay = createdDay;
        this.expiresDay = expiresDay;
        this.councilVotes = new LinkedHashMap<>();
        if (councilVotes != null) {
            for (final String seat : new String[]{"merchants", "commons", "nobility", "scholars"}) {
                if (councilVotes.containsKey(seat)) {
                    this.councilVotes.put(seat, Boolean.TRUE.equals(councilVotes.get(seat)));
                }
            }
        }
        this.status = Objects.requireNonNullElse(status, Status.OPEN);
    }

    static ParliamentProposal restore(
            final long id,
            final String proposer,
            final int previousTaxRate,
            final int proposedTaxRate,
            final long createdDay,
            final long expiresDay,
            final Map<String, Boolean> councilVotes,
            final Status status) {
        return new ParliamentProposal(id, proposer, previousTaxRate, proposedTaxRate,
                createdDay, expiresDay, councilVotes, status);
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
        final Map<String, Boolean> votes = new LinkedHashMap<>();

        // These are transparent first-pass political heuristics, not random dice.
        // Later milestones can make each seat an independently modeled faction.
        votes.put("merchants",
                proposedTaxRate <= currentTaxRate || proposedTaxRate <= 5);
        votes.put("commons",
                proposedTaxRate < currentTaxRate
                        || (policy == EconomicPolicy.WELFARE
                            && stability >= 55
                            && proposedTaxRate <= currentTaxRate + 5));
        votes.put("nobility", proposedTaxRate <= currentTaxRate + 1);
        votes.put("scholars",
                proposedTaxRate <= currentTaxRate
                        || (policy == EconomicPolicy.WELFARE && stability >= 60)
                        || stability >= 80);

        return new ParliamentProposal(
                id, proposer, currentTaxRate, proposedTaxRate, safeDay, expiry, votes, Status.OPEN);
    }

    public long id() {
        return id;
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

    public long createdDay() {
        return createdDay;
    }

    public long expiresDay() {
        return expiresDay;
    }

    public Status status() {
        return status;
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

    /**
     * Applies a recorded Emperor assent or veto. Passing requires a majority
     * of the four council seats plus the Emperor's affirmative vote (3 of 5).
     */
    Status resolveByEmperor(final boolean emperorAssents, final long currentDay, final int currentTaxRate) {
        if (!isOpen()) {
            return status;
        }
        if (currentDay > expiresDay) {
            status = Status.EXPIRED;
            return status;
        }
        if (!emperorAssents
                || proposedTaxRate == currentTaxRate
                || councilYesVotes() + 1 < REQUIRED_MAJORITY
                || councilVotes.size() < TOTAL_SEATS - 1) {
            status = Status.REJECTED;
            return status;
        }
        status = Status.PASSED;
        return status;
    }

    boolean expireIfDue(final long currentDay) {
        if (isOpen() && currentDay > expiresDay) {
            status = Status.EXPIRED;
            return true;
        }
        return false;
    }

    public String statusId() {
        return status.name().toLowerCase(Locale.ROOT);
    }
}
