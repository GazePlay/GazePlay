package net.gazeplay.games.connect4;

import net.gazeplay.commons.random.ReplayablePseudoRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * The computer of "Quatre en ligne" (maintainer's choices of 10/11/2026), at 3 levels:
 * <ul>
 * <li>easy: it wins if it can, else it plays at random;</li>
 * <li>normal: it wins if it can, blocks a line of 4 of the player, never gives the player a win on top of its token
 * when it can avoid it, and likes the middle;</li>
 * <li>hard: it looks several moves ahead (search of the best move, alpha-beta, with a score of the lines of 4 still
 * open).</li>
 * </ul>
 * Its choices among equal moves are drawn with the random generator of the game: the same in a replay.
 */
final class Connect4Computer {

    /**
     * The moves looked ahead by the hard computer.
     */
    static final int DEPTH = 6;
    private static final int WIN = 1_000_000;
    /**
     * The columns from the middle out, for the search.
     */
    private static final int[] ORDER = {3, 2, 4, 1, 5, 0, 6};

    private final Connect4Opponent level;
    private final ReplayablePseudoRandom random;

    Connect4Computer(final Connect4Opponent level, final ReplayablePseudoRandom random) {
        this.level = level;
        this.random = random;
    }

    /**
     * @return the column the computer plays, as the player "me" against "other"
     */
    int choose(final Connect4Board board, final int me, final int other) {
        final List<Integer> playable = board.playable();
        // Win at once, at every level
        for (final int column : playable) {
            if (board.wins(column, me)) {
                return column;
            }
        }
        return switch (level) {
            case HARD -> search(board, me, other);
            case NORMAL -> careful(board, me, other);
            default -> playable.get(random.nextInt(playable.size()));
        };
    }

    /**
     * Blocks a win of the other player, else avoids giving one, and likes the middle.
     */
    private int careful(final Connect4Board board, final int me, final int other) {
        final List<Integer> playable = board.playable();
        for (final int column : playable) {
            if (board.wins(column, other)) {
                return column;
            }
        }
        final List<Integer> safe = new ArrayList<>();
        for (final int column : playable) {
            board.drop(column, me);
            if (!board.wins(column, other)) {
                safe.add(column);
            }
            board.undo(column);
        }
        final List<Integer> choices = safe.isEmpty() ? playable : safe;
        // Drawn with a weight of 4 in the middle column, down to 1 at the sides
        int total = 0;
        for (final int column : choices) {
            total += 4 - Math.abs(column - 3);
        }
        int draw = random.nextInt(total);
        for (final int column : choices) {
            draw -= 4 - Math.abs(column - 3);
            if (draw < 0) {
                return column;
            }
        }
        return choices.get(choices.size() - 1);
    }

    /**
     * The best move found by looking {@link #DEPTH} moves ahead; among equal ones, one drawn at random.
     */
    private int search(final Connect4Board board, final int me, final int other) {
        int best = Integer.MIN_VALUE;
        final List<Integer> bests = new ArrayList<>();
        for (final int column : ORDER) {
            if (!board.canPlay(column)) {
                continue;
            }
            final int row = board.drop(column, me);
            final int score = board.lineThrough(column, row, me) ? WIN
                : -negamax(board, DEPTH - 1, -WIN * 2, WIN * 2, other, me);
            board.undo(column);
            if (score > best) {
                best = score;
                bests.clear();
                bests.add(column);
            } else if (score == best) {
                bests.add(column);
            }
        }
        return bests.get(random.nextInt(bests.size()));
    }

    /**
     * @return the score of the grid for the player to play, looking some moves ahead
     */
    private int negamax(final Connect4Board board, final int depth, int alpha, final int beta, final int player,
                        final int other) {
        if (board.isFull()) {
            return 0;
        }
        if (depth == 0) {
            return evaluate(board, player, other);
        }
        int best = -WIN * 2;
        for (final int column : ORDER) {
            if (!board.canPlay(column)) {
                continue;
            }
            final int row = board.drop(column, player);
            // A sooner win is better
            final int score = board.lineThrough(column, row, player) ? WIN + depth
                : -negamax(board, depth - 1, -beta, -alpha, other, player);
            board.undo(column);
            if (score > best) {
                best = score;
            }
            if (best > alpha) {
                alpha = best;
            }
            if (alpha >= beta) {
                break;
            }
        }
        return best;
    }

    /**
     * @return a score of the lines of 4 still open: those with 2 or 3 tokens of a player and none of the other one,
     * and the tokens in the middle column
     */
    static int evaluate(final Connect4Board board, final int player, final int other) {
        int score = 0;
        for (int row = 0; row < Connect4Board.ROWS; row++) {
            if (board.get(3, row) == player) {
                score += 3;
            } else if (board.get(3, row) == other) {
                score -= 3;
            }
        }
        final int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (final int[] direction : directions) {
            for (int column = 0; column < Connect4Board.COLUMNS; column++) {
                for (int row = 0; row < Connect4Board.ROWS; row++) {
                    final int lastColumn = column + 3 * direction[0];
                    final int lastRow = row + 3 * direction[1];
                    if (lastColumn >= Connect4Board.COLUMNS || lastRow < 0 || lastRow >= Connect4Board.ROWS) {
                        continue;
                    }
                    int mine = 0;
                    int theirs = 0;
                    for (int k = 0; k < 4; k++) {
                        final int cell = board.get(column + k * direction[0], row + k * direction[1]);
                        if (cell == player) {
                            mine++;
                        } else if (cell == other) {
                            theirs++;
                        }
                    }
                    if (theirs == 0) {
                        score += mine == 3 ? 50 : mine == 2 ? 10 : 0;
                    } else if (mine == 0) {
                        score -= theirs == 3 ? 60 : theirs == 2 ? 10 : 0;
                    }
                }
            }
        }
        return score;
    }
}
