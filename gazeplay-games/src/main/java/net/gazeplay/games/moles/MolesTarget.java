package net.gazeplay.games.moles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * What comes out of the holes of the moles game (maintainer's choices of 4/11/2026): moles drawn for the game, the
 * Biboules of the shooting game, or the images of the user, in a circle (a variant of their own: the game used to take
 * the default images without saying so when the user had none).
 */
@Getter
@RequiredArgsConstructor
public enum MolesTarget {
    MOLES("MolesMoles"),
    BIBOULES("MolesBiboules"),
    MY_IMAGES("MolesMyImages");

    /**
     * The folder of the images of the user of the game.
     */
    public static final String USER_IMAGES_FOLDER = UserImagesVariant.IMAGES;

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the target of a key, the moles for an unknown one
     */
    public static MolesTarget of(final String key) {
        for (final MolesTarget target : values()) {
            if (target.key.equals(key)) {
                return target;
            }
        }
        return MOLES;
    }
}
