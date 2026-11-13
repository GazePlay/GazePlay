package net.gazeplay.games.ladder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * The pictures at the bottom of the ladder of "Échelle" (13/11/2026): the animals of the default images, or the images
 * of the user (a variant of their own). A switch of the variants.
 */
@Getter
@RequiredArgsConstructor
public enum LadderPictures {
    ANIMALS("LadderAnimals"),
    MY_IMAGES("LadderMyImages");

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
    public static LadderPictures of(final String key) {
        for (final LadderPictures pictures : values()) {
            if (pictures.key.equals(key)) {
                return pictures;
            }
        }
        return ANIMALS;
    }
}
