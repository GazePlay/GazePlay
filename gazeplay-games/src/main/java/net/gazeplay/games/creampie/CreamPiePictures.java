package net.gazeplay.games.creampie;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * The pictures of the targets of "Tartes à la crème" (maintainer's choice of 12/11/2026): the animals of the default
 * images, or the images of the user (photos of the family or of the school: the spirit of the game; a variant of their
 * own, the game used to show them instead of the animals without saying so).
 */
@Getter
@RequiredArgsConstructor
public enum CreamPiePictures {
    ANIMALS("PieAnimals"),
    MY_IMAGES("PieMyImages");

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
    public static CreamPiePictures of(final String key) {
        for (final CreamPiePictures pictures : values()) {
            if (pictures.key.equals(key)) {
                return pictures;
            }
        }
        return ANIMALS;
    }
}
