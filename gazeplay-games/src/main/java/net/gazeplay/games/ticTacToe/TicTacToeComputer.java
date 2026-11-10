package net.gazeplay.games.ticTacToe;

import net.gazeplay.commons.random.ReplayablePseudoRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * The computer of the Morpion (maintainer's choices of 10/11/2026), at 3 levels:
 * <ul>
 * <li>easy: it wins if it can, else it plays at random;</li>
 * <li>medium: it wins if it can, else it blocks a line of the player, else it takes the middle, else a corner;</li>
 * <li>hard: it plays the best move (a perfect computer never loses at the Morpion), but makes a mistake about once in
 * five moves, so that it can be beaten (maintainer's choice A; measured on 10/11/2026: against a perfect player, it
 * loses 419 games out of 1000 and draws the others).</li>
 * </ul>
 * Its choices among equal moves are drawn with the random generator of the game: the same in a replay.
 */
final class TicTacToeComputer {

    /**
     * How often the hard computer makes a mistake.
     */
    static final double MISTAKES = 0.2;
    private static final int[] CORNERS = {0, 2, 6, 8};

    private final TicTacToeOpponent level;
    private final ReplayablePseudoRandom random;
    private final double mistakes;

    TicTacToeComputer(final TicTacToeOpponent level, final ReplayablePseudoRandom random) {
        this(level, random, MISTAKES);
    }

    /**
     * @param mistakes how often the hard computer makes a mistake (0 for the tests: a perfect computer)
     */
    TicTacToeComputer(final TicTacToeOpponent level, final ReplayablePseudoRandom random, final double mistakes) {
        this.level = level;
        this.random = random;
        this.mistakes = mistakes;
    }

    /**
     * @return the box the computer plays, as the player "me" against "other"
     */
    int choose(final TicTacToeBoard board, final int me, final int other) {
        final List<Integer> free = board.free();
        for (final int box : free) {
            if (board.wins(box, me)) {
                return box;
            }
        }
        return switch (level) {
            case HARD -> best(board, me, other);
            case NORMAL -> careful(board, me, other);
            default -> free.get(random.nextInt(free.size()));
        };
    }

    private int careful(final TicTacToeBoard board, final int me, final int other) {
        final List<Integer> free = board.free();
        for (final int box : free) {
            if (board.wins(box, other)) {
                return box;
            }
        }
        if (board.canPlay(4)) {
            return 4;
        }
        final List<Integer> corners = new ArrayList<>();
        for (final int corner : CORNERS) {
            if (board.canPlay(corner)) {
                corners.add(corner);
            }
        }
        final List<Integer> choices = corners.isEmpty() ? free : corners;
        return choices.get(random.nextInt(choices.size()));
    }

    /**
     * The best moves by a full search; about once in five moves, another one (a mistake), if there is one.
     */
    private int best(final TicTacToeBoard board, final int me, final int other) {
        int bestScore = Integer.MIN_VALUE;
        final List<Integer> bests = new ArrayList<>();
        final List<Integer> others = new ArrayList<>();
        final List<Integer> free = board.free();
        final int[] scores = new int[9];
        for (final int box : free) {
            board.play(box, me);
            scores[box] = -negamax(board, other, me);
            board.undo(box);
            bestScore = Math.max(bestScore, scores[box]);
        }
        for (final int box : free) {
            (scores[box] == bestScore ? bests : others).add(box);
        }
        if (!others.isEmpty() && random.nextDouble() < mistakes) {
            return others.get(random.nextInt(others.size()));
        }
        return bests.get(random.nextInt(bests.size()));
    }

    /**
     * @return the score of the grid for the player to play: 10 minus the moves to a win, 0 for a draw, negative for a
     * loss
     */
    private static int negamax(final TicTacToeBoard board, final int player, final int other) {
        if (board.winner() == other) {
            return -(10 - board.count());
        }
        if (board.isFull()) {
            return 0;
        }
        int best = Integer.MIN_VALUE;
        for (final int box : board.free()) {
            board.play(box, player);
            best = Math.max(best, -negamax(board, other, player));
            board.undo(box);
        }
        return best;
    }
}
