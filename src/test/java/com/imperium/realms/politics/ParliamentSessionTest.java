package com.imperium.realms.politics;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ParliamentSessionTest {
    @Test
    void proposalRequiresAFullInGameDayBeforeResolution() {
        final ParliamentSession session = new ParliamentSession();
        final UUID proposer = UUID.randomUUID();

        assertTrue(session.propose(ImperialLaw.PUBLIC_WORKS_ACT, proposer, 4L));
        assertTrue(session.proposalLawId().isPresent());
        assertTrue(session.resolve(4L).isEmpty());
        assertTrue(session.resolve(5L).isPresent());
    }

    @Test
    void eachPlayerCanVoteOnlyOnce() {
        final ParliamentSession session = new ParliamentSession();
        final UUID proposer = UUID.randomUUID();
        final UUID voter = UUID.randomUUID();

        assertTrue(session.propose(ImperialLaw.SCHOLARSHIP_CHARTER, proposer, 1L));
        assertTrue(session.castVote(voter, true));
        assertFalse(session.castVote(voter, false));
        assertEquals(1, session.yesVotes());
        assertEquals(0, session.noVotes());
    }

    @Test
    void majorityPassesAndEnactsLaw() {
        final ParliamentSession session = new ParliamentSession();
        final UUID proposer = UUID.randomUUID();
        final UUID yesOne = UUID.randomUUID();
        final UUID yesTwo = UUID.randomUUID();
        final UUID noOne = UUID.randomUUID();

        assertTrue(session.propose(ImperialLaw.PUBLIC_WORKS_ACT, proposer, 0L));
        assertTrue(session.castVote(yesOne, true));
        assertTrue(session.castVote(yesTwo, true));
        assertTrue(session.castVote(noOne, false));

        final ParliamentSession.Resolution resolution = session.resolve(1L).orElseThrow();
        assertTrue(resolution.passed());
        assertEquals(2, resolution.yesVotes());
        assertEquals(1, resolution.noVotes());
        assertTrue(session.isEnacted(ImperialLaw.PUBLIC_WORKS_ACT));
        assertFalse(session.proposalLawId().isPresent());
        assertFalse(session.propose(ImperialLaw.PUBLIC_WORKS_ACT, proposer, 2L));
    }

    @Test
    void tieAndEmptyVoteFail() {
        final ParliamentSession empty = new ParliamentSession();
        final UUID proposer = UUID.randomUUID();
        assertTrue(empty.propose(ImperialLaw.TAX_RELIEF_CHARTER, proposer, 0L));
        assertFalse(empty.resolve(1L).orElseThrow().passed());

        final ParliamentSession tied = new ParliamentSession();
        assertTrue(tied.propose(ImperialLaw.TAX_RELIEF_CHARTER, proposer, 2L));
        assertTrue(tied.castVote(UUID.randomUUID(), true));
        assertTrue(tied.castVote(UUID.randomUUID(), false));
        assertFalse(tied.resolve(3L).orElseThrow().passed());
    }
}
