package com.imperium.realms.politics;

import com.imperium.realms.colony.ColonyIdentity;

import java.util.Objects;

/**
 * Persistent bilateral relationship between two real MineColonies colonies.
 */
public final class DiplomaticRelation {
    private final ColonyIdentity first;
    private final ColonyIdentity second;
    private int standing;
    private TreatyType treaty;
    private long lastChangedGameDay;

    public DiplomaticRelation(
            final ColonyIdentity colonyA,
            final ColonyIdentity colonyB,
            final int standing,
            final TreatyType treaty,
            final long lastChangedGameDay) {
        Objects.requireNonNull(colonyA, "colonyA");
        Objects.requireNonNull(colonyB, "colonyB");
        if (colonyA.equals(colonyB)) {
            throw new IllegalArgumentException("A colony cannot have a diplomatic relation with itself");
        }
        if (colonyA.storageKey().compareTo(colonyB.storageKey()) < 0) {
            first = colonyA;
            second = colonyB;
        } else {
            first = colonyB;
            second = colonyA;
        }
        this.standing = clamp(standing, -100, 100);
        this.treaty = Objects.requireNonNull(treaty, "treaty");
        this.lastChangedGameDay = Math.max(0L, lastChangedGameDay);
    }

    public ColonyIdentity first() {
        return first;
    }

    public ColonyIdentity second() {
        return second;
    }

    public int standing() {
        return standing;
    }

    public TreatyType treaty() {
        return treaty;
    }

    public long lastChangedGameDay() {
        return lastChangedGameDay;
    }

    public String stanceId() {
        if (standing <= -50) {
            return "hostile";
        }
        if (standing < 0) {
            return "strained";
        }
        if (standing < 20) {
            return "neutral";
        }
        if (standing < 60) {
            return "friendly";
        }
        return "allied";
    }

    public ColonyIdentity otherColony(final ColonyIdentity identity) {
        if (first.equals(identity)) {
            return second;
        }
        if (second.equals(identity)) {
            return first;
        }
        throw new IllegalArgumentException("Colony is not part of this relation");
    }

    public void applyTreaty(final TreatyType newTreaty, final long gameDay) {
        treaty = Objects.requireNonNull(newTreaty, "newTreaty");
        standing = Math.max(standing, newTreaty.initialStanding());
        lastChangedGameDay = Math.max(0L, gameDay);
    }

    public Snapshot snapshotFor(final ColonyIdentity identity) {
        return new Snapshot(otherColony(identity), standing, treaty, stanceId(), lastChangedGameDay);
    }

    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Snapshot(
            ColonyIdentity otherColony,
            int standing,
            TreatyType treaty,
            String stanceId,
            long lastChangedGameDay) {
    }
}
