package net.gazeplay.games.ticTacToe;

import java.util.ArrayList;
import java.util.List;

/**
 * The grid of the Morpion (name code TicTacToe): 9 boxes, numbered from 0 (top left) to 8 (bottom right), line by line;
 * 3 marks of a player in a line, a column or a diagonal win.
 */
final class TicTacToeBoard {

    static final int EMPTY = 0;
    static final int[][] LINES = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
        {0, 4, 8}, {2, 4, 6}
    };

    private final int[] cells = new int[9];
    private int count;

    int get(final int box) {
        return cells[box];
    }

    boolean canPlay(final int box) {
        return box >= 0 && box < 9 && cells[box] == EMPTY;
    }

    /**
     * @return the free boxes, in order
     */
    List<Integer> free() {
        final List<Integer> boxes = new ArrayList<>();
        for (int box = 0; box < 9; box++) {
            if (cells[box] == EMPTY) {
                boxes.add(box);
            }
        }
        return boxes;
    }

    void play(final int box, final int player) {
        if (!canPlay(box)) {
            throw new IllegalArgumentException("Box taken " + box);
        }
        cells[box] = player;
        count++;
    }

    void undo(final int box) {
        cells[box] = EMPTY;
        count--;
    }

    boolean isFull() {
        return count == 9;
    }

    int count() {
        return count;
    }

    void clear() {
        for (int box = 0; box < 9; box++) {
            cells[box] = EMPTY;
        }
        count = 0;
    }

    /**
     * @return the 3 boxes of a line of a player, null if none
     */
    int[] winningLine() {
        for (final int[] line : LINES) {
            if (cells[line[0]] != EMPTY && cells[line[0]] == cells[line[1]] && cells[line[0]] == cells[line[2]]) {
                return line;
            }
        }
        return null;
    }

    /**
     * @return the player with a line of 3, {@link #EMPTY} if none
     */
    int winner() {
        final int[] line = winningLine();
        return line == null ? EMPTY : cells[line[0]];
    }

    /**
     * @return whether a player wins by playing a box (the grid is left as it was)
     */
    boolean wins(final int box, final int player) {
        if (!canPlay(box)) {
            return false;
        }
        cells[box] = player;
        final boolean won = winner() == player;
        cells[box] = EMPTY;
        return won;
    }
}
