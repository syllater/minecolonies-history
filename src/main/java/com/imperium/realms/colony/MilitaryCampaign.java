package com.imperium.realms.colony;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * A persistent, time-delayed strategic operation between two real MineColonies
 * colonies. Operations resolve deterministically on the server and never
 * directly transfer ownership of buildings or territory.
 */
public final class MilitaryCampaign {
    public enum Type {
        BORDER_PATROL("border_patrol", 25L, 0L, 1L, 8, 0),
        RELIEF_EXPEDITION("relief_expedition", 75L, 5L, 2L, 0, 0),
        WAR_CAMPAIGN("war_campaign", 150L, 10L, 3L, 0, 3);

        private final String id;
        private final long crownCost;
        private final long influenceCost;
        private final long durationDays;
        private final int readinessBonus;
        private final long minimumTrainingPoints;

        Type(
                final String id,
                final long crownCost,
                final long influenceCost,
                final long durationDays,
                final int readinessBonus,
                final long minimumTrainingPoints) {
            this.id = id;
            this.crownCost = crownCost;
            this.influenceCost = influenceCost;
            this.durationDays = durationDays;
            this.readinessBonus = readinessBonus;
            this.minimumTrainingPoints = minimumTrainingPoints;
        }

        public String id() {
            return id;
        }

        public long crownCost() {
            return crownCost;
        }

        public long influenceCost() {
            return influenceCost;
        }

        public long durationDays() {
            return durationDays;
        }

        int readinessBonus() {
            return readinessBonus;
        }

        public long minimumTrainingPoints() {
            return minimumTrainingPoints;
        }

        public static Optional<Type> fromId(final String id) {
            if (id == null) {
                return Optional.empty();
            }
            return Arrays.stream(values())
                    .filter(type -> type.id.equalsIgnoreCase(id.trim()))
                    .findFirst();
        }
    }

    public enum Outcome {
        PENDING,
        SUCCESS,
        STALEMATE,
        DEFEAT
    }

    private final long id;
    private final Type type;
    private final ColonyIdentity targetIdentity;
    private final String targetName;
    private final String commander;
    private final long startedDay;
    private final long resolvesDay;
    private final int launchReadiness;
    private Outcome outcome;
    private long resolvedDay;

    private MilitaryCampaign(
            final long id,
            final Type type,
            final ColonyIdentity targetIdentity,
            final String targetName,
            final String commander,
            final long startedDay,
            final long resolvesDay,
            final int launchReadiness,
            final Outcome outcome,
            final long resolvedDay) {
        if (id < 1L || startedDay < 0L || resolvesDay < startedDay) {
            throw new IllegalArgumentException("Invalid campaign identity or timing");
        }
        this.id = id;
        this.type = Objects.requireNonNull(type, "type");
        this.targetIdentity = Objects.requireNonNull(targetIdentity, "targetIdentity");
        this.targetName = normalize(targetName, "Unknown colony");
        this.commander = normalize(commander, "Unknown commander");
        this.startedDay = startedDay;
        this.resolvesDay = resolvesDay;
        this.launchReadiness = Math.max(0, Math.min(1_000, launchReadiness));
        this.outcome = Objects.requireNonNullElse(outcome, Outcome.PENDING);
        this.resolvedDay = Math.max(-1L, resolvedDay);
        if (this.outcome == Outcome.PENDING) {
            this.resolvedDay = -1L;
        } else if (this.resolvedDay < this.startedDay) {
            this.resolvedDay = this.resolvesDay;
        }
    }

    static MilitaryCampaign start(
            final long id,
            final Type type,
            final ColonyIdentity targetIdentity,
            final String targetName,
            final String commander,
            final long startedDay,
            final int launchReadiness) {
        final long safeDay = Math.max(0L, startedDay);
        final long duration = type.durationDays();
        final long resolvesDay = safeDay > Long.MAX_VALUE - duration
                ? Long.MAX_VALUE : safeDay + duration;
        return new MilitaryCampaign(id, type, targetIdentity, targetName, commander,
                safeDay, resolvesDay, launchReadiness, Outcome.PENDING, -1L);
    }

    static MilitaryCampaign restore(
            final long id,
            final Type type,
            final ColonyIdentity targetIdentity,
            final String targetName,
            final String commander,
            final long startedDay,
            final long resolvesDay,
            final int launchReadiness,
            final Outcome outcome,
            final long resolvedDay) {
        return new MilitaryCampaign(id, type, targetIdentity, targetName, commander,
                startedDay, resolvesDay, launchReadiness, outcome, resolvedDay);
    }

    public long id() {
        return id;
    }

    public Type type() {
        return type;
    }

    public ColonyIdentity targetIdentity() {
        return targetIdentity;
    }

    public String targetName() {
        return targetName;
    }

    public String commander() {
        return commander;
    }

    public long startedDay() {
        return startedDay;
    }

    public long resolvesDay() {
        return resolvesDay;
    }

    public int launchReadiness() {
        return launchReadiness;
    }

    public Outcome outcome() {
        return outcome;
    }

    public long resolvedDay() {
        return resolvedDay;
    }

    public boolean isPending() {
        return outcome == Outcome.PENDING;
    }

    public boolean isDue(final long currentDay) {
        return isPending() && currentDay >= resolvesDay;
    }

    /**
     * Resolve once when the campaign's due day is reached. The operation is
     * deterministic so servers and save/reload cycles produce the same result.
     */
    Outcome resolveIfDue(final long currentDay, final int targetReadiness, final int relationScore) {
        if (!isDue(currentDay)) {
            return outcome;
        }

        int ownScore = launchReadiness + type.readinessBonus();
        if (type == Type.WAR_CAMPAIGN) {
            ownScore += relationScore <= -50 ? 8 : relationScore <= -15 ? 4
                    : relationScore >= 40 ? -4 : 0;
        } else if (relationScore >= 40) {
            ownScore += 3;
        }
        ownScore = Math.max(0, Math.min(2_000, ownScore));
        final int opposingScore = Math.max(0, Math.min(1_000, targetReadiness));

        if (type == Type.WAR_CAMPAIGN) {
            if (ownScore >= opposingScore + 5) {
                outcome = Outcome.SUCCESS;
            } else if (ownScore + 5 >= opposingScore) {
                outcome = Outcome.STALEMATE;
            } else {
                outcome = Outcome.DEFEAT;
            }
        } else if (ownScore >= opposingScore) {
            outcome = Outcome.SUCCESS;
        } else if (ownScore + 5 >= opposingScore) {
            outcome = Outcome.STALEMATE;
        } else {
            outcome = Outcome.DEFEAT;
        }
        resolvedDay = Math.max(startedDay, currentDay);
        return outcome;
    }

    private static String normalize(final String value, final String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
