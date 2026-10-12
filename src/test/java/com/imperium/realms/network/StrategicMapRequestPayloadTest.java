package com.imperium.realms.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class StrategicMapRequestPayloadTest {
    @Test
    void emptyRequestDefaultsToServerSelectedCapitalDimension() {
        assertEquals("", new StrategicMapRequestPayload().dimensionId());
        assertEquals("", new StrategicMapRequestPayload("  ").dimensionId());
        assertEquals("minecraft:the_nether",
                new StrategicMapRequestPayload(" minecraft:the_nether ").dimensionId());
    }

    @Test
    void dimensionRequestIsBounded() {
        final StrategicMapRequestPayload payload =
                new StrategicMapRequestPayload("d".repeat(StrategicMapRequestPayload.MAX_DIMENSION_LENGTH + 50));

        assertEquals(StrategicMapRequestPayload.MAX_DIMENSION_LENGTH, payload.dimensionId().length());
    }
}
