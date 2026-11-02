package net.gazeplay.games.bubbles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What the bubbles of "Bubbles" show (2/11/2026): colors, sea animals, or the images given by the user (folder
 * images/Bubbles: the hidden pictures are then the user's images too).
 */
@Getter
@RequiredArgsConstructor
public enum BubblesContent {
    COLORS("BubblesColors"),
    ANIMALS("BubblesAnimals"),
    MY_IMAGES("BubblesMyImages");

    /**
     * The folder of the images of the user.
     */
    public static final String USER_IMAGES_FOLDER = "Bubbles";

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the content of a key, colors for an unknown one
     */
    public static BubblesContent of(final String key) {
        for (final BubblesContent content : values()) {
            if (content.key.equals(key)) {
                return content;
            }
        }
        return COLORS;
    }
}
