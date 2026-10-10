package com.imperium.realms.colony;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Small deterministic renderer for strategic maps printed in chat.
 *
 * <p>Lower Z is north/top; higher X is east/right. Coordinates are normalized
 * to the bounds of the points provided. If several colonies share one grid
 * cell, the cell is marked with '*'.</p>
 */
public final class StrategicMapGrid {
    public static final int DEFAULT_WIDTH = 25;
    public static final int DEFAULT_HEIGHT = 9;
    private static final char EMPTY_CELL = '.';
    private static final char COLLISION_CELL = '*';

    private StrategicMapGrid() {
    }

    public record Point(char marker, int x, int z) {
    }

    public static List<String> render(final List<Point> points, final int width, final int height) {
        Objects.requireNonNull(points, "points");
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }

        final char[][] grid = new char[height][width];
        for (final char[] row : grid) {
            Arrays.fill(row, EMPTY_CELL);
        }
        if (points.isEmpty()) {
            return rows(grid);
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (final Point point : points) {
            Objects.requireNonNull(point, "points must not contain null");
            minX = Math.min(minX, point.x());
            maxX = Math.max(maxX, point.x());
            minZ = Math.min(minZ, point.z());
            maxZ = Math.max(maxZ, point.z());
        }

        for (final Point point : points) {
            final int column = maxX == minX ? width / 2
                    : (int) Math.round((point.x() - (double) minX)
                            * (width - 1.0) / (maxX - (double) minX));
            final int row = maxZ == minZ ? height / 2
                    : (int) Math.round((point.z() - (double) minZ)
                            * (height - 1.0) / (maxZ - (double) minZ));
            if (grid[row][column] == EMPTY_CELL) {
                grid[row][column] = point.marker();
            } else {
                grid[row][column] = COLLISION_CELL;
            }
        }

        return rows(grid);
    }

    private static List<String> rows(final char[][] grid) {
        final List<String> result = new ArrayList<>(grid.length);
        for (final char[] row : grid) {
            result.add(new String(row));
        }
        return List.copyOf(result);
    }
}
