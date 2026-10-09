package com.imperium.realms.government;

import com.imperium.realms.economy.ImperialPolicy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A persisted parliament bill proposing a change to the colony's tax policy.
 * One vote per eligible MineColonies owner/officer UUID is accepted.
 */
public final class PolicyBill {
    public enum Status {
        OPEN("open"),
        PASSED("passed"),
        REJECTED("rejected");

        private final String id;
        Status(final String id) { this.id = id; }
        public String id() { return id; }
        public String translationKey() { return "imperium.bill." + id; }

        public static Status fromId(final String id) {
            if (id == null) return REJECTED;
            for (Status status : values()) {
                if (status.id.equalsIgnoreCase(id)) return status;
            }
            return REJECTED;
        }
    }

    public enum VoteResult {
        RECORDED,
        PASSED,
        REJECTED,
        ALREADY_VOTED,
        NOT_ELIGIBLE,
        NOT_OPEN
    }

    private final int id;
    private final ImperialPolicy proposedPolicy;
    private final UUID proposer;
    private final long createdGameTime;
    private Status status;
    private final Map<UUID, Boolean> votes;

    public PolicyBill(
            final int id,
            final ImperialPolicy proposedPolicy,
            final UUID proposer,
            final long createdGameTime,
            final Status status,
            final Map<UUID, Boolean> votes) {
        if (id < 1) throw new IllegalArgumentException("Bill IDs must be positive");
        this.id = id;
        this.proposedPolicy = Objects.requireNonNull(proposedPolicy, "proposedPolicy");
        this.proposer = Objects.requireNonNull(proposer, "proposer");
        this.createdGameTime = Math.max(0L, createdGameTime);
        this.status = Objects.requireNonNullElse(status, Status.OPEN);
        this.votes = new LinkedHashMap<>(votes == null ? Map.of() : votes);
    }

    public int id() { return id; }
    public ImperialPolicy proposedPolicy() { return proposedPolicy; }
    public UUID proposer() { return proposer; }
    public long createdGameTime() { return createdGameTime; }
    public Status status() { return status; }
    public Map<UUID, Boolean> votes() { return Map.copyOf(votes); }

    public int yesVotes() {
        return (int) votes.values().stream().filter(Boolean::booleanValue).count();
    }

    public int noVotes() {
        return (int) votes.values().stream().filter(vote -> !vote).count();
    }

    /**
     * Register an eligible player's vote and resolve the bill only when a
     * strict majority of the current electorate has voted yes or the electorate
     * has fully voted without a yes majority.
     */
    public VoteResult castVote(final UUID voter, final boolean approve, final Set<UUID> electorate) {
        Objects.requireNonNull(voter, "voter");
        Objects.requireNonNull(electorate, "electorate");
        if (status != Status.OPEN) return VoteResult.NOT_OPEN;
        if (!electorate.contains(voter)) return VoteResult.NOT_ELIGIBLE;
        if (votes.containsKey(voter)) return VoteResult.ALREADY_VOTED;

        votes.put(voter, approve);
        final int yes = yesVotes();
        final int requiredMajority = electorate.size() / 2 + 1;
        if (yes >= requiredMajority) {
            status = Status.PASSED;
            return VoteResult.PASSED;
        }
        if (votes.size() >= electorate.size()) {
            status = Status.REJECTED;
            return VoteResult.REJECTED;
        }
        return VoteResult.RECORDED;
    }
}
