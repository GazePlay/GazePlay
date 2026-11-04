package net.gazeplay.games.ninja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * What "Ninja" cuts (maintainer's choices of 4/11/2026): the fruits drawn for the game, or the images of the user, in a
 * circle (a variant of their own: the game used to show them instead of its images without saying so).
 */
@Getter
@RequiredArgsConstructor
public enum NinjaTarget {
    FRUITS("NinjaFruits"),
    MY_IMAGES("NinjaMyImages");

    /**
     * The folder of the images of the user of the game.
     */
    public static final String USER_IMAGES_FOLDER = UserImagesVariant.IMAGES;

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the target of a key, the fruits for an unknown one
     */
    public static NinjaTarget of(final String key) {
        for (final NinjaTarget target : values()) {
            if (target.key.equals(key)) {
                return target;
            }
        }
        return FRUITS;
    }
}
