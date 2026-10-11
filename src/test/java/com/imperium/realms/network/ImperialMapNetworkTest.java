package com.imperium.realms.network;

import com.imperium.realms.colony.ColonyIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ImperialMapNetworkTest {
    private static final ColonyIdentity CAPITAL =
            new ColonyIdentity("minecraft:overworld", 1);
    private static final ColonyIdentity NETHER_PROVINCE =
            new ColonyIdentity("minecraft:the_nether", 4);
    private static final ColonyIdentity END_PROVINCE =
            new ColonyIdentity("minecraft:the_end", 9);

    @Test
    void selectsOnlyADimensionContainingARealmProvince() {
        assertEquals("minecraft:the_nether",
                ImperialMapNetwork.resolveMapDimension(
                        "minecraft:the_nether", CAPITAL,
                        List.of(CAPITAL, NETHER_PROVINCE)));
        assertEquals("minecraft:the_end",
                ImperialMapNetwork.resolveMapDimension(
                        "minecraft:the_end", CAPITAL,
                        List.of(CAPITAL, END_PROVINCE)));
    }

    @Test
    void emptyOrUnrelatedDimensionRequestsFallBackToCapital() {
        final List<ColonyIdentity> provinces = List.of(CAPITAL, NETHER_PROVINCE);

        assertEquals("minecraft:overworld",
                ImperialMapNetwork.resolveMapDimension("", CAPITAL, provinces));
        assertEquals("minecraft:overworld",
                ImperialMapNetwork.resolveMapDimension("  ", CAPITAL, provinces));
        assertEquals("minecraft:overworld",
                ImperialMapNetwork.resolveMapDimension("minecraft:the_end", CAPITAL, provinces));
        assertEquals("minecraft:overworld",
                ImperialMapNetwork.resolveMapDimension(null, CAPITAL, provinces));
    }

    @Test
    void requestIsNormalizedBeforeMembershipCheck() {
        assertEquals("minecraft:the_nether",
                ImperialMapNetwork.resolveMapDimension(
                        " minecraft:the_nether ", CAPITAL, List.of(CAPITAL, NETHER_PROVINCE)));
    }
}
