package net.gazeplay.games.connect4;

import java.util.ArrayList;
import java.util.List;

/**
 * The grid of "Quatre en ligne" (name code Connect4): 7 columns of 6 boxes; a token dropped in a column falls on the
 * lowest free box; 4 tokens of a player in a line, a column or a diagonal win. Rows are counted from the bottom.
 */
final class Connect4Board {

    static final int COLUMNS = 7;
    static final int ROWS = 6;
    static final int EMPTY = 0;

    private final int[][] cells = new int[COLUMNS][ROWS];
    private final int[] heights = new int[COLUMNS];
    private int count;

    /**
     * @return the player (1 or 2) of a box, {@link #EMPTY} if none
     */
    int get(final int column, final int row) {
        return cells[column][row];
    }

    /**
     * @return the number of tokens in a column
     */
    int height(final int column) {
        return heights[column];
    }

    boolean canPlay(final int column) {
        return column >= 0 && column < COLUMNS && heights[column] < ROWS;
    }

    /**
     * @return the columns that are not full, from the left
     */
    List<Integer> playable() {
        final List<Integer> columns = new ArrayList<>();
        for (int column = 0; column < COLUMNS; column++) {
            if (canPlay(column)) {
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * Drops a token of a player in a column.
     *
     * @return the row where it stops
     */
    int drop(final int column, final int player) {
        if (!canPlay(column)) {
            throw new IllegalArgumentException("Full column " + column);
        }
        final int row = heights[column]++;
        cells[column][row] = player;
        count++;
        return row;
    }

    /**
     * Takes back the last token of a column.
     */
    void undo(final int column) {
        final int row = --heights[column];
        cells[column][row] = EMPTY;
        count--;
    }

    boolean isFull() {
        return count == COLUMNS * ROWS;
    }

    int count() {
        return count;
    }

    void clear() {
        for (int column = 0; column < COLUMNS; column++) {
            for (int row = 0; row < ROWS; row++) {
                cells[column][row] = EMPTY;
            }
            heights[column] = 0;
        }
        count = 0;
    }

    /**
     * @return the 4 boxes ({column, row}) of a line of 4 tokens of the same player, null if none
     */
    int[][] winningLine() {
        final int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (final int[] direction : directions) {
            for (int column = 0; column < COLUMNS; column++) {
                for (int row = 0; row < ROWS; row++) {
                    final int player = cells[column][row];
                    if (player == EMPTY) {
                        continue;
                    }
                    final int lastColumn = column + 3 * direction[0];
                    final int lastRow = row + 3 * direction[1];
                    if (lastColumn >= COLUMNS || lastRow < 0 || lastRow >= ROWS) {
                        continue;
                    }
                    boolean four = true;
                    for (int k = 1; k < 4 && four; k++) {
                        four = cells[column + k * direction[0]][row + k * direction[1]] == player;
                    }
                    if (four) {
                        final int[][] line = new int[4][];
                        for (int k = 0; k < 4; k++) {
                            line[k] = new int[]{column + k * direction[0], row + k * direction[1]};
                        }
                        return line;
                    }
                }
            }
        }
        return null;
    }

    /**
     * @return the player with a line of 4, {@link #EMPTY} if none
     */
    int winner() {
        final int[][] line = winningLine();
        return line == null ? EMPTY : cells[line[0][0]][line[0][1]];
    }

    /**
     * @return whether a player wins by dropping a token in a column (the grid is left as it was)
     */
    boolean wins(final int column, final int player) {
        if (!canPlay(column)) {
            return false;
        }
        final int row = drop(column, player);
        final boolean won = lineThrough(column, row, player);
        undo(column);
        return won;
    }

    /**
     * @return whether the token of a box is in a line of 4 of its player
     */
    boolean lineThrough(final int column, final int row, final int player) {
        final int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (final int[] direction : directions) {
            int length = 1;
            for (final int sign : new int[]{1, -1}) {
                int c = column + sign * direction[0];
                int r = row + sign * direction[1];
                while (c >= 0 && c < COLUMNS && r >= 0 && r < ROWS && cells[c][r] == player) {
                    length++;
                    c += sign * direction[0];
                    r += sign * direction[1];
                }
            }
            if (length >= 4) {
                return true;
            }
        }
        return false;
    }
}
