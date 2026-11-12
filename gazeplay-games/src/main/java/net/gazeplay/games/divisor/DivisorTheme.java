package net.gazeplay.games.divisor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * What the targets of "Diviseur" show (maintainer's choice of 12/11/2026, when "Lapins" became a theme of the game):
 * the rabbits drawn for the game, the animals of the default images, or the images of the user (a variant of their
 * own: the game used to show them instead of the animals without saying so).
 */
@Getter
@RequiredArgsConstructor
public enum DivisorTheme {
    RABBITS("DivisorRabbits"),
    ANIMALS("DivisorAnimals"),
    MY_IMAGES("DivisorMyImages");

    /**
     * The folder of the images of the user of the game.
     */
    public static final String USER_IMAGES_FOLDER = UserImagesVariant.IMAGES;

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the theme of a key, the rabbits for an unknown one
     */
    public static DivisorTheme of(final String key) {
        for (final DivisorTheme theme : values()) {
            if (theme.key.equals(key)) {
                return theme;
            }
        }
        return RABBITS;
    }
}
