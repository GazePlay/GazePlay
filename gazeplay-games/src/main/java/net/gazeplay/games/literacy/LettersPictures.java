package net.gazeplay.games.literacy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * The pictures hidden under the cards of "Identifier les lettres" (maintainer's choice of 12/11/2026): the animals of
 * the default images, or the images of the user (a variant of their own: the game used to show them instead of the
 * animals without saying so).
 */
@Getter
@RequiredArgsConstructor
public enum LettersPictures {
    ANIMALS("LettersAnimals"),
    MY_IMAGES("LettersMyImages");

    /**
     * The folder of the images of the user of the game.
     */
    public static final String USER_IMAGES_FOLDER = UserImagesVariant.IMAGES;

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the pictures of a key, the animals for an unknown one
     */
    public static LettersPictures of(final String key) {
        for (final LettersPictures pictures : values()) {
            if (pictures.key.equals(key)) {
                return pictures;
            }
        }
        return ANIMALS;
    }
}
