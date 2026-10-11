package com.imperium.realms.network;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MapSnapshotRateLimiterTest {
    @Test
    void limitsRepeatedRequestsByOnePlayer() {
        final MapSnapshotRateLimiter limiter = new MapSnapshotRateLimiter();
        final UUID player = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertTrue(limiter.tryAcquire(player, 1_000L));
        assertFalse(limiter.tryAcquire(player, 1_000L));
        assertFalse(limiter.tryAcquire(player, 1_009L));
        assertTrue(limiter.tryAcquire(player, 1_010L));
    }

    @Test
    void playersHaveIndependentCooldowns() {
        final MapSnapshotRateLimiter limiter = new MapSnapshotRateLimiter();
        final UUID playerA = UUID.fromString("00000000-0000-0000-0000-000000000001");
        final UUID playerB = UUID.fromString("00000000-0000-0000-0000-000000000002");

        assertTrue(limiter.tryAcquire(playerA, 2_000L));
        assertTrue(limiter.tryAcquire(playerB, 2_000L));
        assertFalse(limiter.tryAcquire(playerA, 2_001L));
        assertEquals(2, limiter.trackedPlayers());
    }

    @Test
    void expiresInactivePlayersAndAllowsRequestsAfterClockReset() {
        final MapSnapshotRateLimiter limiter = new MapSnapshotRateLimiter();
        final UUID player = UUID.fromString("00000000-0000-0000-0000-000000000001");
        final UUID otherPlayer = UUID.fromString("00000000-0000-0000-0000-000000000002");

        assertTrue(limiter.tryAcquire(player, 100L));
        // Pruning happens every 200 ticks and removes a one-day-old entry.
        // The time is past the prune deadline, but not an exact 200-tick multiple.
        assertTrue(limiter.tryAcquire(otherPlayer, 24_201L));
        assertEquals(1, limiter.trackedPlayers());

        // Integrated-server worlds may restart their game clock in one JVM.
        assertTrue(limiter.tryAcquire(player, 10L));
    }
}
