package com.imperium.realms.politics;

/**
 * The four domestic power blocs tracked by the initial political simulation.
 */
public enum FactionType {
    CROWN_LOYALISTS("crown_loyalists", "faction.imperium.crown_loyalists"),
    MERCHANTS_GUILD("merchants_guild", "faction.imperium.merchants_guild"),
    COMMONERS_ASSEMBLY("commoners_assembly", "faction.imperium.commoners_assembly"),
    SCHOLARS_CIRCLE("scholars_circle", "faction.imperium.scholars_circle");

    private final String id;
    private final String translationKey;

    FactionType(final String id, final String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    public String savedDataKey() {
        return "faction_" + id;
    }
}
