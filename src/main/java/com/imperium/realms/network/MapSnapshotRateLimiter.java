package com.imperium.realms.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Per-player request limiter for map snapshots.
 *
 * <p>Network callbacks execute on the server thread. The synchronized methods
 * also make the utility safe if the networking implementation changes its
 * callback scheduling in a later platform version.</p>
 */
final class MapSnapshotRateLimiter {
    static final long COOLDOWN_TICKS = 10L;
    private static final long PRUNE_INTERVAL_TICKS = 200L;
    private static final long ENTRY_TTL_TICKS = 24_000L;

    private final Map<UUID, Long> lastAcceptedTick = new HashMap<>();
    private long nextPruneTick;
    private long lastObservedTick = -1L;

    /**
     * Allows at most one request for a player per cooldown window.
     *
     * @param playerId stable authenticated server-side player UUID
     * @param serverTick overworld game time, shared across dimensions
     * @return true when the request may build a map snapshot
     */
    synchronized boolean tryAcquire(final UUID playerId, final long serverTick) {
        Objects.requireNonNull(playerId, "playerId");
        final long now = Math.max(0L, serverTick);

        // Prune when an interval has elapsed, not only when a request happens
        // on an exact tick multiple (players may request at a fixed offset).
        if (lastObservedTick > now || now >= nextPruneTick) {
            pruneExpiredEntries(now);
            nextPruneTick = now > Long.MAX_VALUE - PRUNE_INTERVAL_TICKS
                    ? Long.MAX_VALUE : now + PRUNE_INTERVAL_TICKS;
        }
        lastObservedTick = now;

        final Long previousTick = lastAcceptedTick.get(playerId);
        if (previousTick != null && now >= previousTick
                && now - previousTick < COOLDOWN_TICKS) {
            return false;
        }

        // If the server's game clock moved backwards during an integrated-server
        // restart, treat the new clock as a fresh session for this player.
        lastAcceptedTick.put(playerId, now);
        return true;
    }

    private void pruneExpiredEntries(final long now) {
        final Iterator<Map.Entry<UUID, Long>> iterator = lastAcceptedTick.entrySet().iterator();
        while (iterator.hasNext()) {
            final long lastTick = iterator.next().getValue();
            if (now < lastTick || now - lastTick >= ENTRY_TTL_TICKS) {
                iterator.remove();
            }
        }
    }

    synchronized int trackedPlayers() {
        return lastAcceptedTick.size();
    }
}
