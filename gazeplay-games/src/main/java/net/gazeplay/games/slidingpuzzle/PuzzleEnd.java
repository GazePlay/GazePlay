package net.gazeplay.games.slidingpuzzle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the sliding puzzle ends (maintainer's choice of 9/11/2026): after 1, 3 or 5 puzzles solved, or never.
 */
@Getter
@RequiredArgsConstructor
public enum PuzzleEnd {
    SOLVED_1("PuzzleSolved1", 1),
    SOLVED_3("PuzzleSolved3", 3),
    SOLVED_5("PuzzleSolved5", 5),
    ENDLESS("PuzzleEndless", 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The puzzles to solve, 0 for a game without end.
     */
    private final int puzzles;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static PuzzleEnd of(final String key) {
        for (final PuzzleEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
