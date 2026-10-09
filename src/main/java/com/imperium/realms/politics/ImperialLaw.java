package com.imperium.realms.politics;

import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.economy.EmpirePolicy;

import java.util.Arrays;
import java.util.Optional;

/**
 * Legislation available in the first parliament iteration.
 *
 * <p>Passing an act applies its initial effect to the persistent economic state.
 * The enacted act remains in the record for history and future systems.</p>
 */
public enum ImperialLaw {
    PUBLIC_WORKS_ACT("public_works_act"),
    SCHOLARSHIP_CHARTER("scholarship_charter"),
    TAX_RELIEF_CHARTER("tax_relief_charter");

    private final String id;

    ImperialLaw(final String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "law.imperium." + id;
    }

    public void apply(final EmpireStateSavedData economy, final ColonyIdentity identity) {
        switch (this) {
            case PUBLIC_WORKS_ACT -> economy.setPolicy(identity, EmpirePolicy.PUBLIC_WORKS);
            case SCHOLARSHIP_CHARTER -> economy.setPolicy(identity, EmpirePolicy.SCHOLARSHIP);
            case TAX_RELIEF_CHARTER -> economy.get(identity).ifPresent(state -> {
                if (state.taxRatePercent() > 15) {
                    economy.setTaxRate(identity, 15);
                }
            });
        }
    }

    public static Optional<ImperialLaw> fromId(final String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(law -> law.id.equalsIgnoreCase(id))
                .findFirst();
    }
}
