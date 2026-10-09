package com.imperium.realms.politics;

/**
 * Government traditions supported by the realm simulation.
 */
public enum GovernmentType {
    CONSTITUTIONAL_EMPIRE("government.imperium.constitutional_empire");

    private final String translationKey;

    GovernmentType(final String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
