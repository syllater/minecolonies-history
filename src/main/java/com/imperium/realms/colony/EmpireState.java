package com.imperium.realms.colony;

import java.util.Objects;

/**
 * Imperium-owned state for one MineColonies colony.
 *
 * <p>This model deliberately does not store anything in MineColonies' private
 * colony NBT. It is kept in Imperium's own world SavedData and can be expanded
 * with treasury, laws, government and diplomacy in later milestones.</p>
 */
public final class EmpireState {
    private final ColonyIdentity identity;
    private String colonyName;
    private final long firstSeenGameTime;
    private long lastSeenGameTime;

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.colonyName = normalizeName(colonyName);
        this.firstSeenGameTime = Math.max(0L, firstSeenGameTime);
        this.lastSeenGameTime = Math.max(this.firstSeenGameTime, lastSeenGameTime);
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

        // A scan may happen every few seconds. Persist a heartbeat at most once
        // per in-game day to avoid marking SavedData dirty on every scan.
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

    private static String normalizeName(final String name) {
        if (name == null || name.isBlank()) {
            return "Unnamed colony";
        }
        return name.trim();
    }
}
