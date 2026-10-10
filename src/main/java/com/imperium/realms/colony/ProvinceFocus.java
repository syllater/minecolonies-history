package com.imperium.realms.colony;

import java.util.Arrays;
import java.util.Optional;

/**
 * Specialization for an abstract province backed by a real MineColonies
 * settlement. Focus effects are deterministic and applied by the server's
 * daily turn or by the corresponding worker progression hook.
 */
public enum ProvinceFocus {
    AGRICULTURE("agriculture", 5, 0, 0, -1, 25),
    TRADE("trade", 15, 0, 0, 0, 25),
    SCHOLARSHIP("scholarship", 0, 1, 0, 0, 30),
    MILITARY("military", 0, 0, 0, 0, 25),
    CIVIC("civic", 0, 0, 2, -2, 25);

    private final String id;
    private final int taxBonusPercent;
    private final int dailyKnowledgeBonus;
    private final int dailyStabilityBonus;
    private final int dailyUnrestAdjustment;
    private final int developmentPerInvestment;

    ProvinceFocus(
            final String id,
            final int taxBonusPercent,
            final int dailyKnowledgeBonus,
            final int dailyStabilityBonus,
            final int dailyUnrestAdjustment,
            final int developmentPerInvestment) {
        this.id = id;
        this.taxBonusPercent = taxBonusPercent;
        this.dailyKnowledgeBonus = dailyKnowledgeBonus;
        this.dailyStabilityBonus = dailyStabilityBonus;
        this.dailyUnrestAdjustment = dailyUnrestAdjustment;
        this.developmentPerInvestment = developmentPerInvestment;
    }

    public String id() {
        return id;
    }

    int taxBonusPercent() {
        return taxBonusPercent;
    }

    int dailyKnowledgeBonus() {
        return dailyKnowledgeBonus;
    }

    int dailyStabilityBonus() {
        return dailyStabilityBonus;
    }

    int dailyUnrestAdjustment() {
        return dailyUnrestAdjustment;
    }

    int developmentPerInvestment() {
        return developmentPerInvestment;
    }

    public static Optional<ProvinceFocus> fromId(final String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(focus -> focus.id.equalsIgnoreCase(id.trim()))
                .findFirst();
    }
}
