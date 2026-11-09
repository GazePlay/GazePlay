package net.gazeplay.games.slidingpuzzle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * What the pieces of the sliding puzzle show (maintainer's choices of 9/11/2026): numbers drawn by the game, a picture
 * of the game (a different one at each round), or an image of the user (a variant of its own).
 */
@Getter
@RequiredArgsConstructor
public enum PuzzleContent {
    NUMBERS("PuzzleNumbers"),
    PICTURES("PuzzlePictures"),
    MY_IMAGES("PuzzleMyImages");

    /**
     * The folder of the images of the user of the game.
     */
    public static final String USER_IMAGES_FOLDER = UserImagesVariant.IMAGES;

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
