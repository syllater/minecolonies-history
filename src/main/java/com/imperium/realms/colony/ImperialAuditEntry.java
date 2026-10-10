package com.imperium.realms.colony;

import java.util.Locale;

/** A bounded, persistent audit entry for an imperial realm. */
public record ImperialAuditEntry(long dayIndex, String actor, String actionId, String subject, long amount) {
    public ImperialAuditEntry {
        if (dayIndex < 0L || amount < 0L) {
            throw new IllegalArgumentException("Audit day and amount must be non-negative");
        }
        actor = clean(actor, 32, "Unknown");
        actionId = clean(actionId, 32, "unknown").toLowerCase(Locale.ROOT);
        if (!actionId.matches("[a-z0-9_-]+")) actionId = "unknown";
        subject = clean(subject, 96, "-");
    }

    private static String clean(final String value, final int maxLength, final String fallback) {
        final String cleaned = value == null ? "" : value.replaceAll("\\p{Cntrl}", "").trim();
        if (cleaned.isEmpty()) return fallback;
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }
}
