package com.imperium.realms.network;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class StrategicMapDataPayloadTest {
    @Test
    void payloadTruncatesUntrustedTextAndCapsProvinceList() {
        final List<StrategicMapDataPayload.ProvinceView> provinces = new ArrayList<>();
        for (int index = 0; index < StrategicMapDataPayload.MAX_PROVINCES + 10; index++) {
            provinces.add(new StrategicMapDataPayload.ProvinceView(
                    "N".repeat(120),
                    "d".repeat(150),
                    index,
                    index * 100,
                    -index * 100,
                    true,
                    index == 0,
                    50,
                    0,
                    75));
        }

        final StrategicMapDataPayload payload = new StrategicMapDataPayload(
                true,
                "k".repeat(200),
                "R".repeat(100),
                "minecraft:overworld",
                0,
                0,
                provinces);

        assertEquals(128, payload.messageKey().length());
        assertEquals(64, payload.realmName().length());
        assertEquals(StrategicMapDataPayload.MAX_PROVINCES, payload.provinces().size());
        assertEquals(96, payload.provinces().get(0).name().length());
        assertEquals(128, payload.provinces().get(0).dimensionId().length());
    }

    @Test
    void provinceMetricsAndIdsAreClamped() {
        final StrategicMapDataPayload.ProvinceView province =
                new StrategicMapDataPayload.ProvinceView(
                        "Colony", "minecraft:overworld", -7, 0, 0, true, false, 180, -10, 140);

        assertEquals(0, province.colonyId());
        assertEquals(100, province.stability());
        assertEquals(0, province.unrest());
        assertEquals(100, province.loyalty());
    }

    @Test
    void provinceListIsImmutableAfterConstruction() {
        final List<StrategicMapDataPayload.ProvinceView> provinces = new ArrayList<>();
        provinces.add(new StrategicMapDataPayload.ProvinceView(
                "Capital", "minecraft:overworld", 1, 0, 0, true, true, 100, 0, 100));
        final StrategicMapDataPayload payload = new StrategicMapDataPayload(
                true, "", "Realm", "minecraft:overworld", 0, 0, provinces);

        provinces.clear();
        assertEquals(1, payload.provinces().size());
        assertThrows(UnsupportedOperationException.class, () -> payload.provinces().clear());
    }
}
