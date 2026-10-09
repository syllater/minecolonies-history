package com.imperium.realms.politics;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Pure model for the ceremonial and executive office of emperor in one colony.
 */
public final class EmperorOffice {
    private UUID emperorId;
    private String emperorName;
    private long appointmentGameDay;
    private long reignCount;
    private int abdications;

    public EmperorOffice() {
        this(null, null, 0L, 0L, 0);
    }

    public EmperorOffice(
            final UUID emperorId,
            final String emperorName,
            final long appointmentGameDay,
            final long reignCount,
            final int abdications) {
        this.emperorId = emperorId;
        this.emperorName = emperorId == null ? null : cleanName(emperorName);
        this.appointmentGameDay = Math.max(0L, appointmentGameDay);
        this.reignCount = Math.max(0L, reignCount);
        this.abdications = Math.max(0, abdications);
    }

    public Optional<UUID> emperorId() {
        return Optional.ofNullable(emperorId);
    }

    public Optional<String> emperorName() {
        return Optional.ofNullable(emperorName);
    }

    public long appointmentGameDay() {
        return appointmentGameDay;
    }

    public long reignCount() {
        return reignCount;
    }

    public int abdications() {
        return abdications;
    }

    public boolean isEmperor(final UUID playerId) {
        return emperorId != null && emperorId.equals(playerId);
    }

    public boolean claim(final UUID claimant, final String claimantName, final long gameDay) {
        Objects.requireNonNull(claimant, "claimant");
        if (emperorId != null) {
            return false;
        }
        enthrone(claimant, claimantName, gameDay);
        return true;
    }

    public boolean appoint(
            final UUID actor,
            final UUID successor,
            final String successorName,
            final long gameDay) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(successor, "successor");
        if (!isEmperor(actor) || actor.equals(successor)) {
            return false;
        }
        enthrone(successor, successorName, gameDay);
        return true;
    }

    public boolean abdicate(final UUID actor) {
        Objects.requireNonNull(actor, "actor");
        if (!isEmperor(actor)) {
            return false;
        }
        emperorId = null;
        emperorName = null;
        appointmentGameDay = 0L;
        abdications++;
        return true;
    }

    private void enthrone(final UUID playerId, final String playerName, final long gameDay) {
        emperorId = playerId;
        emperorName = cleanName(playerName);
        appointmentGameDay = Math.max(0L, gameDay);
        reignCount++;
    }

    private static String cleanName(final String name) {
        if (name == null || name.isBlank()) {
            return "Unknown emperor";
        }
        return name.trim();
    }
}
