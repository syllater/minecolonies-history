package com.imperium.realms.politics;

import com.imperium.realms.colony.ColonyIdentity;

import java.util.Objects;
import java.util.UUID;

/**
 * Pending bilateral treaty offer, indexed by its target colony.
 */
public record DiplomacyOffer(
        ColonyIdentity source,
        ColonyIdentity target,
        TreatyType treaty,
        UUID proposerId,
        String proposerName,
        long proposedGameDay) {
    public DiplomacyOffer {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(treaty, "treaty");
        Objects.requireNonNull(proposerId, "proposerId");
        if (source.equals(target)) {
            throw new IllegalArgumentException("A colony cannot offer a treaty to itself");
        }
        proposerName = proposerName == null || proposerName.isBlank() ? "Unknown envoy" : proposerName.trim();
        proposedGameDay = Math.max(0L, proposedGameDay);
    }
}
