package net.gazeplay.commons.utils.stats;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * HeatMapGrid was extracted from Stats; before the switch, a test fed 2000 pseudo-random samples (seed 42, some out of
 * the scene) to both and found identical counts (see MODERNIZATION.md, step 8).
 */
class HeatMapGridTest {

    private static String csv(HeatMapGrid grid) {
        StringWriter text = new StringWriter();
        try (PrintWriter out = new PrintWriter(text)) {
            grid.writeCsv(out);
        }
        return text.toString();
    }

    @Test
    void shouldIncrementADiskAroundTheSample() {
        HeatMapGrid grid = new HeatMapGrid(100, 50, 2);

        grid.add(50, 20);

        double[][] counts = grid.counts();
        assertEquals(25, counts.length);
        assertEquals(50, counts[0].length);
        assertEquals(1, counts[10][25]);
        // Radius of 10 cells, strictly
        assertEquals(1, counts[10][25 + 9]);
        assertEquals(0, counts[10][25 + 10]);
    }

    @Test
    void shouldIgnoreTheCellsOutsideOfTheGrid() {
        HeatMapGrid grid = new HeatMapGrid(10, 10, 1);

        grid.add(0, 0);
        grid.add(-50, 500);

        assertEquals(1, grid.counts()[0][0]);
    }

    @Test
    void shouldWriteTheCsvFormat() {
        HeatMapGrid grid = new HeatMapGrid(3, 2, 1);
        grid.counts()[0][1] = 2.7;
        grid.counts()[1][2] = 5;
        String eol = System.lineSeparator();

        assertEquals("0, 2, 0" + eol + "0, 0, 5" + eol, csv(grid));
    }
}
