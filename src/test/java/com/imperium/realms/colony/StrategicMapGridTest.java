package com.imperium.realms.colony;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class StrategicMapGridTest {
    @Test
    void emptyGridHasRequestedDimensions() {
        final List<String> rows = StrategicMapGrid.render(List.of(), 8, 3);

        assertEquals(3, rows.size());
        assertEquals(List.of("........", "........", "........"), rows);
    }

    @Test
    void northIsAtTheTopAndEastIsToTheRight() {
        final List<String> rows = StrategicMapGrid.render(
                List.of(
                        new StrategicMapGrid.Point('H', -100, -100),
                        new StrategicMapGrid.Point('A', 100, 100)),
                5, 3);

        assertEquals("H....", rows.get(0));
        assertEquals(".....", rows.get(1));
        assertEquals("....A", rows.get(2));
    }

    @Test
    void coincidentColonyCentersAreMarkedAsCollision() {
        final List<String> rows = StrategicMapGrid.render(
                List.of(
                        new StrategicMapGrid.Point('H', 20, 20),
                        new StrategicMapGrid.Point('A', 20, 20)),
                5, 3);

        assertEquals(".....", rows.get(0));
        assertEquals("..*..", rows.get(1));
        assertEquals(".....", rows.get(2));
    }

    @Test
    void singlePointIsCenteredOnDegenerateAxes() {
        final List<String> rows = StrategicMapGrid.render(
                List.of(new StrategicMapGrid.Point('H', 0, 0)),
                5, 3);

        assertEquals(".....", rows.get(0));
        assertEquals("..H..", rows.get(1));
        assertEquals(".....", rows.get(2));
    }

    @Test
    void dimensionsMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> StrategicMapGrid.render(List.of(), 0, 3));
        assertThrows(IllegalArgumentException.class,
                () -> StrategicMapGrid.render(List.of(), 5, -1));
    }
}
