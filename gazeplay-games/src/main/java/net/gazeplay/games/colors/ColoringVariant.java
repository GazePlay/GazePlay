package net.gazeplay.games.colors;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * Variants of the game "Coloring": the coloring pages of a level, named after their number of zones to fill (easy: 1 or 2
 * zones, medium: 3 to 6, hard: 7 or more), or the images given by the user in the settings ("My drawings"). The palette
 * is the same everywhere: they were named after its size, which did not change (tester's feedback of 24/11/2026, point
 * 72). The names of the values are kept: they are written in the saved games.
 */
public enum ColoringVariant implements UserImagesVariant {
    EASY("easy", "ColoringEasy"),
    NORMAL("normal", "ColoringNormal"),
    HARD("hard", "ColoringHard"),
    CUSTOM(null, "ColoringMyDrawings");

    /**
     * Folder of the images given by the user.
     */
    static final String USER_IMAGES_FOLDER = UserImagesVariant.COLORING;

    /**
     * Folder of the coloring pages of the level, in data/colors/images; null for the images of the user.
     */
    @Getter
    private final String folder;

    /**
     * Key of the translation of the name of the variant.
     */
    @Getter
    private final String label;

    ColoringVariant(final String folder, final String label) {
        this.folder = folder;
        this.label = label;
    }

    @Override
    public String getUserImagesFolder() {
        return folder == null ? USER_IMAGES_FOLDER : null;
    }
}
