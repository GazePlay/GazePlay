package net.gazeplay.games.blocs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * The pictures under the blocks of "Blocs" (13/11/2026): the animals of the default images, or the images of the user
 * (a variant of their own: the game used to show them instead of the animals without saying so). A switch of the
 * variants.
 */
@Getter
@RequiredArgsConstructor
public enum BlocsPictures {
    ANIMALS("BlocsAnimals"),
    MY_IMAGES("BlocsMyImages");

    /**
     * The folder of the images of the user of the game.
     */
    public static final String USER_IMAGES_FOLDER = UserImagesVariant.IMAGES;

    /**
     * Key of translation, and value of the switch.
     */
    private final String key;

    /**
     * @return the pictures of a key, the animals for an unknown one
     */
    public static BlocsPictures of(final String key) {
        for (final BlocsPictures pictures : values()) {
            if (pictures.key.equals(key)) {
                return pictures;
            }
        }
        return ANIMALS;
    }
}
