package com.imperium.realms.colony;

/** Persistent appointment of a player as governor of one realm province. */
public record ProvinceGovernor(String playerUuid, String playerName, long appointedDay) {
    public ProvinceGovernor {
        playerUuid = clean(playerUuid, 64, "unknown");
        playerName = clean(playerName, 32, "Unknown Governor");
        if (appointedDay < 0L) {
            throw new IllegalArgumentException("Appointment day must not be negative");
        }
    }

    private static String clean(final String value, final int maxLength, final String fallback) {
        final String safe = value == null ? "" : value.replaceAll("\\p{Cntrl}", "").trim();
        if (safe.isEmpty()) return fallback;
        return safe.length() <= maxLength ? safe : safe.substring(0, maxLength);
    }
}
