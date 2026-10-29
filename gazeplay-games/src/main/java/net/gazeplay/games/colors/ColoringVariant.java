package net.gazeplay.games.colors;

import lombok.Getter;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

/**
 * Variants of the game "Coloring": the coloring pages of a level, given by their number of zones to fill, shown as the
 * size of the palette (small: 1 or 2 zones, medium: 3 to 6, large: 7 or more), or the images given by the user in the
 * settings (custom palette, folder images/Coloring). The names of the values are kept: they are written in the saved
 * games.
 */
public enum ColoringVariant implements UserImagesVariant {
    EASY("easy", "SmallPalette"),
    NORMAL("normal", "MediumPalette"),
    HARD("hard", "LargePalette"),
    CUSTOM(null, "CustomPalette");

    /**
     * Folder of the images given by the user for the custom palette, in the images folder.
     */
    static final String USER_IMAGES_FOLDER = "Coloring";

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
