package net.gazeplay.games.sprintFinish;

import lombok.Getter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * A map of "Sprint Finish" (2/11/2026): a grid of {@value #COLUMNS} x {@value #ROWS} cells, scaled to the screen by
 * the game. It holds the hedges, the start of the cat, its bowl, the start of the dogs, the buttons with the fences
 * they open, and the logs going up and down a column. Made by {@link SprintMapGenerator}.
 */
@Getter
public class SprintMap {

    public static final int COLUMNS = 16;

    public static final int ROWS = 9;

    /**
     * A cell of the grid.
     */
    public record Cell(int column, int row) {
    }

    /**
     * A button and the fences it opens, drawn in its color (index in the colors of the game).
     */
    public record Button(Cell cell, List<Cell> fences, int color) {
    }

    /**
     * A log going up and down a whole column, {@code length} cells long; {@code phase} (0 to 1) is where it starts.
     */
    public record Log(int column, double length, double phase) {
    }

    private final boolean[][] hedges = new boolean[COLUMNS][ROWS];

    private final Cell cat;

    private final Cell dish;

    private final List<Cell> dogs = new ArrayList<>();

    private final List<Button> buttons = new ArrayList<>();

    private final List<Log> logs = new ArrayList<>();

    SprintMap(final Cell cat, final Cell dish) {
        this.cat = cat;
        this.dish = dish;
    }

    public boolean isHedge(final int column, final int row) {
        return inside(column, row) && hedges[column][row];
    }

    void setHedge(final int column, final int row, final boolean hedge) {
        if (inside(column, row)) {
            hedges[column][row] = hedge;
        }
    }

    public static boolean inside(final int column, final int row) {
        return column >= 0 && column < COLUMNS && row >= 0 && row < ROWS;
    }

    /**
     * @return whether a cell is a fence of a button
     */
    public boolean isFence(final int column, final int row) {
        for (final Button button : buttons) {
            if (button.fences().contains(new Cell(column, row))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return whether a cell is in a column where a log goes
     */
    public boolean isLogColumn(final int column) {
        return logs.stream().anyMatch(log -> log.column() == column);
    }

    /**
     * @param closedFences the fences closed (the others are open)
     * @return the number of steps from a cell to each cell of the grid (up, down, left, right), through the cells
     * without hedge nor closed fence; -1 for a cell that cannot be reached
     */
    public int[][] distancesFrom(final Cell from, final List<Cell> closedFences) {
        final int[][] distances = new int[COLUMNS][ROWS];
        for (final int[] column : distances) {
            Arrays.fill(column, -1);
        }
        if (!inside(from.column(), from.row())) {
            return distances;
        }
        final Deque<Cell> queue = new ArrayDeque<>();
        distances[from.column()][from.row()] = 0;
        queue.add(from);
        final int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            final Cell cell = queue.poll();
            for (final int[] step : steps) {
                final int column = cell.column() + step[0];
                final int row = cell.row() + step[1];
                if (inside(column, row) && distances[column][row] < 0 && !hedges[column][row]
                    && !closedFences.contains(new Cell(column, row))) {
                    distances[column][row] = distances[cell.column()][cell.row()] + 1;
                    queue.add(new Cell(column, row));
                }
            }
        }
        return distances;
    }

    /**
     * @return all the fences of the map
     */
    public List<Cell> allFences() {
        final List<Cell> fences = new ArrayList<>();
        buttons.forEach(button -> fences.addAll(button.fences()));
        return fences;
    }

    /**
     * Whether the map can be played: the cat can reach every button with the fences closed, then its bowl with the
     * fences open; no dog starts on the cat, the bowl, a button, a fence or a log column.
     */
    public boolean isPlayable() {
        final int[][] closed = distancesFrom(cat, allFences());
        for (final Button button : buttons) {
            if (closed[button.cell().column()][button.cell().row()] < 0) {
                return false;
            }
        }
        final int[][] open = distancesFrom(cat, List.of());
        if (open[dish.column()][dish.row()] < 0 || isHedge(cat.column(), cat.row())) {
            return false;
        }
        for (final Cell dog : dogs) {
            if (dog.equals(cat) || dog.equals(dish) || isHedge(dog.column(), dog.row()) || isFence(dog.column(),
                dog.row()) || isLogColumn(dog.column())
                || buttons.stream().anyMatch(button -> button.cell().equals(dog))) {
                return false;
            }
        }
        return true;
    }
}
