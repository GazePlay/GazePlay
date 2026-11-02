package net.gazeplay.games.egg;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What comes out of the eggs of "Egg Game" (2/11/2026): animals born from an egg (OpenMoji), or the images given by
 * the user (folder images/EggGame).
 */
@Getter
@RequiredArgsConstructor
public enum EggContent {
    ANIMALS("EggAnimals"),
    MY_IMAGES("EggMyImages");

    /**
     * The folder of the images of the user.
     */
    public static final String USER_IMAGES_FOLDER = "EggGame";

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the content of a key, animals for an unknown one
     */
    public static EggContent of(final String key) {
        for (final EggContent content : values()) {
            if (content.key.equals(key)) {
                return content;
            }
        }
        return ANIMALS;
    }
}
