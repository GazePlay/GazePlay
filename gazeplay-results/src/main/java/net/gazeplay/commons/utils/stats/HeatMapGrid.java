package net.gazeplay.commons.utils.stats;

import java.io.PrintWriter;

/**
 * Heatmap of a round: number of samples per cell of {@code cellSize} x {@code cellSize} pixels of the scene. Rows
 * follow the Y axis and columns the X axis. Each sample increments the cells of a disk around its position.
 * <p>
 * Extracted from {@code Stats} without any change of computation; free of JavaFX so that it can be tested and reused
 * alone. {@code Stats} keeps three grids per round: the gaze, the mouse, and both (one grid for both before format
 * version 2, anomaly R1).
 */
public final class HeatMapGrid {

    /**
     * Radius of the disk incremented around each sample, in cells.
     */
    static final int TRAIL = 10;

    private final double cellSize;

    private final double[][] counts;

    /**
     * @param cellSize side of a cell, in pixels of the scene (width / 400 since format version 4)
     */
    HeatMapGrid(final double sceneWidth, final double sceneHeight, final double cellSize) {
        this.cellSize = cellSize;
        this.counts = new double[(int) (sceneHeight / cellSize)][(int) (sceneWidth / cellSize)];
    }

    /**
     * Adds a sample at the given position of the scene, in pixels.
     */
    void add(final int x, final int y) {
        final int row = (int) (y / cellSize);
        final int column = (int) (x / cellSize);
        for (int i = -TRAIL; i <= TRAIL; i++) {
            for (int j = -TRAIL; j <= TRAIL; j++) {
                if (Math.sqrt(i * i + j * j) < TRAIL) {
                    increment(row + i, column + j);
                }
            }
        }
    }

    private void increment(final int row, final int column) {
        if (row >= 0 && column >= 0 && row < counts.length && column < counts[0].length) {
            counts[row][column]++;
        }
    }

    /**
     * @return the counts, by row (Y) then column (X); the array itself, not a copy
     */
    double[][] counts() {
        return counts;
    }

    /**
     * Writes heatmap.csv: one line per row, the counts as integers separated by ", ", lines ended by
     * {@link PrintWriter#println()} (system line separator). Persistent format: do not change.
     */
    void writeCsv(final PrintWriter out) {
        for (final double[] row : counts) {
            for (int j = 0; j < counts[0].length - 1; j++) {
                out.print((int) row[j]);
                out.print(", ");
            }
            out.print((int) row[row.length - 1]);
            out.println("");
        }
    }
}
