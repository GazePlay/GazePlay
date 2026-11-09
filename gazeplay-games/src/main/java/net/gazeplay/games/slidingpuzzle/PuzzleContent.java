package net.gazeplay.games.slidingpuzzle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What the pieces of the sliding puzzle show (maintainer's choices of 9/11/2026): numbers drawn by the game, or a
 * picture of the game (a different one at each round). No "My images" (maintainer's decision after his trial of
 * 9/11/2026): an image of the user with transparent parts gives pieces that cannot be chosen.
 */
@Getter
@RequiredArgsConstructor
public enum PuzzleContent {
    NUMBERS("PuzzleNumbers"),
    PICTURES("PuzzlePictures");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the content of a key, the pictures for an unknown one
     */
    public static PuzzleContent of(final String key) {
        for (final PuzzleContent content : values()) {
            if (content.key.equals(key)) {
                return content;
            }
        }
        return PICTURES;
    }
}
