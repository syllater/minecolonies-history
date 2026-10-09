package com.imperium.realms.government;

/** Political interests tracked by each imperial parliament. */
public enum ImperialFaction {
    CROWN("crown", 65),
    GUILDS("guilds", 55),
    COMMONS("commons", 60);

    private final String id;
    private final int defaultSupport;

    ImperialFaction(final String id, final int defaultSupport) {
        this.id = id;
        this.defaultSupport = defaultSupport;
    }

    public String id() { return id; }
    public int defaultSupport() { return defaultSupport; }
    public String translationKey() { return "imperium.faction." + id; }
}
