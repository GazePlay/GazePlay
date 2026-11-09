package net.gazeplay.games.slidingpuzzle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of the sliding puzzle (maintainer's choices of 9/11/2026): the size of the grid, and how much it is shuffled,
 * always by moves that can be played back, so that every puzzle can be solved. A full shuffle of 4 x 4 would be too long
 * to solve with the eyes.
 */
@Getter
@RequiredArgsConstructor
public enum PuzzleLevel {
    EASY("PuzzleEasy", 3, 8),
    NORMAL("PuzzleNormal", 3, 0),
    HARD("PuzzleHard", 4, 40);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The lines and the columns of the grid.
     */
    private final int size;

    /**
     * The moves of the shuffle from the picture rebuilt, 0 for a full shuffle (any position that can be solved).
     */
    private final int moves;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static PuzzleLevel of(final String key) {
        for (final PuzzleLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
