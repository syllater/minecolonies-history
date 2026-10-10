package com.imperium.realms.colony;

import java.util.Arrays;
import java.util.Optional;

/** Deterministic seven-day realm events. */
public enum ImperialRegionalEvent {
    HARVEST_SURPLUS("harvest-surplus", 1, 0, -1, 20L, 0L, 25L),
    MERCHANTS_FAIR("merchants-fair", 1, 1, -1, 25L, 0L, 20L),
    SCHOLARLY_EXCHANGE("scholarly-exchange", 1, 2, 0, -5L, 3L, 0L),
    CIVIC_RECONCILIATION("civic-reconciliation", 2, 3, -5, 0L, 0L, 0L),
    BORDER_TENSIONS("border-tensions", -2, -1, 5, -10L, 0L, -10L),
    WINTER_SHORTAGES("winter-shortages", -3, -2, 6, -15L, -1L, -25L),
    ROYAL_PROGRESS("royal-progress", 2, 2, -2, -10L, 1L, -10L);

    private final String id;
    private final int stabilityDelta;
    private final int legitimacyDelta;
    private final int unrestDelta;
    private final long provincialTreasuryDelta;
    private final long knowledgeDelta;
    private final long imperialTreasuryDelta;

    ImperialRegionalEvent(final String id, final int stabilityDelta, final int legitimacyDelta,
            final int unrestDelta, final long provincialTreasuryDelta, final long knowledgeDelta,
            final long imperialTreasuryDelta) {
        this.id = id;
        this.stabilityDelta = stabilityDelta;
        this.legitimacyDelta = legitimacyDelta;
        this.unrestDelta = unrestDelta;
        this.provincialTreasuryDelta = provincialTreasuryDelta;
        this.knowledgeDelta = knowledgeDelta;
        this.imperialTreasuryDelta = imperialTreasuryDelta;
    }

    public String id() { return id; }
    int stabilityDelta() { return stabilityDelta; }
    int legitimacyDelta() { return legitimacyDelta; }
    int unrestDelta() { return unrestDelta; }
    long provincialTreasuryDelta() { return provincialTreasuryDelta; }
    long knowledgeDelta() { return knowledgeDelta; }
    long imperialTreasuryDelta() { return imperialTreasuryDelta; }

    public static ImperialRegionalEvent forTurn(final long realmId, final long dayIndex) {
        final long mixed = (realmId * 0x9E3779B97F4A7C15L) ^ (dayIndex * 0xBF58476D1CE4E5B9L);
        final int index = (int) Math.floorMod(mixed, (long) values().length);
        return values()[index];
    }

    public static Optional<ImperialRegionalEvent> fromId(final String requestedId) {
        if (requestedId == null) return Optional.empty();
        return Arrays.stream(values()).filter(event -> event.id.equalsIgnoreCase(requestedId.trim())).findFirst();
    }
}
