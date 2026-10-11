package com.imperium.realms.colony;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void viewportKeepsWorldDirectionsAndCoordinateScaleStable() {
        final List<String> rows = StrategicMapGrid.renderViewport(
                List.of(
                        new StrategicMapGrid.Point('W', -1, 0),
                        new StrategicMapGrid.Point('E', 1, 0),
                        new StrategicMapGrid.Point('N', 0, -1),
                        new StrategicMapGrid.Point('S', 0, 1),
                        new StrategicMapGrid.Point('H', 0, 0)),
                5, 3, 0, 0, 1);

        assertEquals("..N..", rows.get(0));
        assertEquals(".WHE.", rows.get(1));
        assertEquals("..S..", rows.get(2));
    }

    @Test
    void viewportZoomAggregatesNearCoordinatesAndPanningMovesThem() {
        final List<StrategicMapGrid.Point> points = List.of(
                new StrategicMapGrid.Point('H', 0, 0),
                new StrategicMapGrid.Point('A', 39, 0),
                new StrategicMapGrid.Point('B', 40, 0));

        final List<String> zoomedOut = StrategicMapGrid.renderViewport(points, 5, 3, 0, 0, 40);
        assertEquals("..*..", zoomedOut.get(1));

        final List<String> panned = StrategicMapGrid.renderViewport(points, 5, 3, 40, 0, 1);
        assertEquals("..B..", panned.get(1));
    }

    @Test
    void viewportOmitsPointsOutsideItsBounds() {
        assertTrue(StrategicMapGrid.isVisibleInViewport(0, 0, 5, 3, 0, 0, 1));
        assertTrue(StrategicMapGrid.isVisibleInViewport(2, 1, 5, 3, 0, 0, 1));
        assertFalse(StrategicMapGrid.isVisibleInViewport(100, 0, 5, 3, 0, 0, 1));
        assertFalse(StrategicMapGrid.isVisibleInViewport(0, -100, 5, 3, 0, 0, 10));
    }

    @Test
    void dimensionsAndZoomScaleMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> StrategicMapGrid.render(List.of(), 0, 3));
        assertThrows(IllegalArgumentException.class,
                () -> StrategicMapGrid.render(List.of(), 5, -1));
        assertThrows(IllegalArgumentException.class,
                () -> StrategicMapGrid.renderViewport(List.of(), 5, 3, 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> StrategicMapGrid.isVisibleInViewport(0, 0, 0, 3, 0, 0, 1));
    }
}
