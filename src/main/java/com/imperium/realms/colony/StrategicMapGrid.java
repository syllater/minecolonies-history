package com.imperium.realms.colony;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Deterministic renderer for strategic maps printed in chat.
 *
 * <p>Lower Z is north/top; higher X is east/right. The overview renderer fits
 * all provided points to the grid. The viewport renderer keeps world coordinates
 * stable, allowing players to pan and zoom by choosing a center and scale.</p>
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
        validate(points, width, height);
        final char[][] grid = blankGrid(width, height);
        if (points.isEmpty()) {
            return rows(grid);
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (final Point point : points) {
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
            put(grid, row, column, point.marker());
        }

        return rows(grid);
    }

    /**
     * Render a stable world-coordinate viewport.
     *
     * @param centerX world X coordinate at the horizontal origin
     * @param centerZ world Z coordinate at the vertical origin
     * @param blocksPerCell zoom scale; each grid cell represents this many blocks
     */
    public static List<String> renderViewport(
            final List<Point> points,
            final int width,
            final int height,
            final int centerX,
            final int centerZ,
            final int blocksPerCell) {
        validate(points, width, height);
        validateScale(blocksPerCell);
        final char[][] grid = blankGrid(width, height);
        for (final Point point : points) {
            final int column = project(point.x(), centerX, blocksPerCell, width);
            final int row = project(point.z(), centerZ, blocksPerCell, height);
            if (column >= 0 && row >= 0) {
                put(grid, row, column, point.marker());
            }
        }
        return rows(grid);
    }

    /**
     * Returns whether a point would be shown in the requested fixed viewport.
     */
    public static boolean isVisibleInViewport(
            final int x,
            final int z,
            final int width,
            final int height,
            final int centerX,
            final int centerZ,
            final int blocksPerCell) {
        validateDimensions(width, height);
        validateScale(blocksPerCell);
        return project(x, centerX, blocksPerCell, width) >= 0
                && project(z, centerZ, blocksPerCell, height) >= 0;
    }

    private static int project(
            final int coordinate,
            final int center,
            final int blocksPerCell,
            final int dimension) {
        final long cellOffset = Math.floorDiv((long) coordinate - center, (long) blocksPerCell);
        final long cell = dimension / 2L + cellOffset;
        return cell >= 0L && cell < dimension ? (int) cell : -1;
    }

    private static char[][] blankGrid(final int width, final int height) {
        final char[][] grid = new char[height][width];
        for (final char[] row : grid) {
            Arrays.fill(row, EMPTY_CELL);
        }
        return grid;
    }

    private static void put(final char[][] grid, final int row, final int column, final char marker) {
        if (row < 0 || row >= grid.length || column < 0 || column >= grid[row].length) {
            return;
        }
        if (grid[row][column] == EMPTY_CELL) {
            grid[row][column] = marker;
        } else {
            grid[row][column] = COLLISION_CELL;
        }
    }

    private static void validate(final List<Point> points, final int width, final int height) {
        Objects.requireNonNull(points, "points");
        validateDimensions(width, height);
        for (final Point point : points) {
            Objects.requireNonNull(point, "points must not contain null");
        }
    }

    private static void validateDimensions(final int width, final int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
    }

    private static void validateScale(final int blocksPerCell) {
        if (blocksPerCell < 1) {
            throw new IllegalArgumentException("blocksPerCell must be positive");
        }
    }

    private static List<String> rows(final char[][] grid) {
        final List<String> result = new ArrayList<>(grid.length);
        for (final char[] row : grid) {
            result.add(new String(row));
        }
        return List.copyOf(result);
    }
}
