package com.imperium.realms.colony;

import java.util.Arrays;
import java.util.Optional;

/**
 * Standing military doctrine for a colony's strategic operations.
 *
 * <p>Offensive posture changes the readiness snapshot when an operation is
 * launched. Defensive posture is evaluated when the operation resolves, along
 * with the target province's current supply-route condition.</p>
 */
public enum MilitaryPosture {
    BALANCED("balanced", 0, 0),
    DEFENSIVE("defensive", 0, 20),
    OFFENSIVE("offensive", 12, 0);

    private final String id;
    private final int launchReadinessBonus;
    private final int defensiveReadinessBonus;

    MilitaryPosture(final String id, final int launchReadinessBonus, final int defensiveReadinessBonus) {
        this.id = id;
        this.launchReadinessBonus = launchReadinessBonus;
        this.defensiveReadinessBonus = defensiveReadinessBonus;
    }

    public String id() {
        return id;
    }

    public int launchReadinessBonus() {
        return launchReadinessBonus;
    }

    public int defensiveReadinessBonus() {
        return defensiveReadinessBonus;
    }

    public static Optional<MilitaryPosture> fromId(final String requestedId) {
        if (requestedId == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(posture -> posture.id.equalsIgnoreCase(requestedId.trim()))
                .findFirst();
    }
}
