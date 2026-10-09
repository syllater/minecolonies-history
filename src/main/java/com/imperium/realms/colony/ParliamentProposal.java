package com.imperium.realms.colony;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

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
    private static final int COUNCIL_MAJORITY = 2;
    private static final int TOTAL_SEATS = 5;
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

    static ParliamentProposal fromNbt(final CompoundTag tag) {
        final Map<String, Boolean> votes = new LinkedHashMap<>();
        if (tag.contains(TAG_VOTES, Tag.TAG_LIST)) {
            final ListTag voteList = tag.getList(TAG_VOTES, Tag.TAG_COMPOUND);
            for (int index = 0; index < voteList.size(); index++) {
                final CompoundTag vote = voteList.getCompound(index);
                final String seat = vote.getString("seat");
                if (seat.equals("merchants") || seat.equals("commons")
                        || seat.equals("nobility") || seat.equals("scholars")) {
                    votes.put(seat, vote.getBoolean("yes"));
                }
            }
        }

        Status loadedStatus = Status.OPEN;
        try {
            loadedStatus = Status.valueOf(tag.getString("status"));
        } catch (IllegalArgumentException ignored) {
            // Unknown status from a newer save is downgraded to an open proposal;
            // it still needs the Emperor's decision and is subject to expiry.
        }

        return new ParliamentProposal(
                tag.getLong("id"),
                tag.getString("proposer"),
                tag.getInt("previous_tax"),
                tag.getInt("proposed_tax"),
                tag.getLong("created_day"),
                tag.getLong("expires_day"),
                votes,
                loadedStatus);
    }

    CompoundTag save() {
        final CompoundTag tag = new CompoundTag();
        tag.putLong("id", id);
        tag.putString("proposer", proposer);
        tag.putInt("previous_tax", previousTaxRate);
        tag.putInt("proposed_tax", proposedTaxRate);
        tag.putLong("created_day", createdDay);
        tag.putLong("expires_day", expiresDay);
        tag.putString("status", status.name());

        final ListTag votes = new ListTag();
        councilVotes.forEach((seat, yes) -> {
            final CompoundTag vote = new CompoundTag();
            vote.putString("seat", seat);
            vote.putBoolean("yes", yes);
            votes.add(vote);
        });
        tag.put(TAG_VOTES, votes);
        return tag;
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
                || councilYesVotes() + 1 < COUNCIL_MAJORITY + 1
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
