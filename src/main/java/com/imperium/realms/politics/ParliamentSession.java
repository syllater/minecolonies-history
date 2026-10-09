package com.imperium.realms.politics;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Pure Java state for one colony's imperial parliament session.
 *
 * <p>Each UUID may vote once per proposal. A proposal cannot be resolved until
 * the next in-game day, allowing colony members time to participate.</p>
 */
public final class ParliamentSession {
    private String proposalLawId;
    private UUID proposer;
    private long proposalGameDay = -1L;
    private final Map<UUID, Boolean> votes = new LinkedHashMap<>();
    private final Set<String> enactedLawIds = new LinkedHashSet<>();

    public ParliamentSession() {
    }

    ParliamentSession(
            final String proposalLawId,
            final UUID proposer,
            final long proposalGameDay,
            final Map<UUID, Boolean> votes,
            final Set<String> enactedLawIds) {
        if (proposalLawId != null && ImperialLaw.fromId(proposalLawId).isPresent() && proposer != null) {
            this.proposalLawId = proposalLawId;
            this.proposer = proposer;
            this.proposalGameDay = Math.max(0L, proposalGameDay);
            this.votes.putAll(votes);
        }
        for (final String lawId : enactedLawIds) {
            if (ImperialLaw.fromId(lawId).isPresent()) {
                this.enactedLawIds.add(lawId);
            }
        }
    }

    public Optional<String> proposalLawId() {
        return Optional.ofNullable(proposalLawId);
    }

    public Optional<UUID> proposer() {
        return Optional.ofNullable(proposer);
    }

    public long proposalGameDay() {
        return proposalGameDay;
    }

    public Map<UUID, Boolean> votes() {
        return Map.copyOf(votes);
    }

    public Set<String> enactedLawIds() {
        return Set.copyOf(enactedLawIds);
    }

    public int yesVotes() {
        return (int) votes.values().stream().filter(Boolean::booleanValue).count();
    }

    public int noVotes() {
        return votes.size() - yesVotes();
    }

    public boolean isEnacted(final ImperialLaw law) {
        return enactedLawIds.contains(Objects.requireNonNull(law, "law").id());
    }

    public boolean propose(final ImperialLaw law, final UUID actor, final long gameDay) {
        Objects.requireNonNull(law, "law");
        Objects.requireNonNull(actor, "actor");
        if (proposalLawId != null || enactedLawIds.contains(law.id())) {
            return false;
        }
        proposalLawId = law.id();
        proposer = actor;
        proposalGameDay = Math.max(0L, gameDay);
        votes.clear();
        return true;
    }

    public boolean castVote(final UUID voter, final boolean inFavor) {
        Objects.requireNonNull(voter, "voter");
        if (proposalLawId == null || votes.containsKey(voter)) {
            return false;
        }
        votes.put(voter, inFavor);
        return true;
    }

    /**
     * Finalize the active proposal if at least one in-game day has elapsed.
     * Ties fail. The enacted law is recorded only for a passing proposal.
     */
    public Optional<Resolution> resolve(final long currentGameDay) {
        if (proposalLawId == null || currentGameDay <= proposalGameDay) {
            return Optional.empty();
        }

        final String lawId = proposalLawId;
        final int yes = yesVotes();
        final int no = noVotes();
        final boolean passed = yes > no && yes > 0;
        if (passed) {
            enactedLawIds.add(lawId);
        }

        proposalLawId = null;
        proposer = null;
        proposalGameDay = -1L;
        votes.clear();
        return Optional.of(new Resolution(lawId, passed, yes, no));
    }

    public record Resolution(String lawId, boolean passed, int yesVotes, int noVotes) {
    }
}
