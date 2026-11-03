package net.gazeplay.commons.gamevariants;

import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A value of the variants of a game ({@link EnumGameVariant}) that plays with images given by the user (decision of the
 * maintainer, 29/10/2026: the images of the user are always a variant of their own). Such a variant gets, without code
 * of its own:
 * <ul>
 * <li>its images, in one folder shared by all the games and all the profiles, {@link #IMAGES}
 * (Documents\GazePlay_Learning\images, maintainer's decision of 3/11/2026: an image is given once for all the games),
 * or, for the drawings to color, {@link #COLORING} (Documents\GazePlay_Learning\coloriages);</li>
 * <li>a setting adding and removing these images (settings, folders);</li>
 * <li>a window of the menu asking for images when it is chosen while the folder is empty.</li>
 * </ul>
 */
public interface UserImagesVariant {

    /**
     * The folder of the images of the user, shared by all the games.
     */
    String IMAGES = GazePlayDirectories.IMAGES_FOLDER;

    /**
     * The folder of the drawings to color of the user (Coloring only: black outlines cut into zones, not photos).
     */
    String COLORING = GazePlayDirectories.COLORING_FOLDER;

    /**
     * @return the folder of the images of the user, {@link #IMAGES} or {@link #COLORING}; null if this value of the
     * variants does not play with images of the user
     */
    String getUserImagesFolder();

    /**
     * @return the folder of the images of the user of a variant, or null if it plays without them
     */
    static String folderOf(final IGameVariant variant) {
        if (variant instanceof EnumGameVariant<?> enumVariant
            && enumVariant.getEnumValue() instanceof UserImagesVariant userImages) {
            return userImages.getUserImagesFolder();
        }
        if (variant instanceof GroupedGameVariant grouped) {
            return grouped.getUserImagesFolder();
        }
        return null;
    }

    /**
     * Extensions of the images that can be given.
     */
    Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "bmp");

    /**
     * @return the folder of the images of the user, in the GazePlay folder: the drawings to color for
     * {@link #COLORING}, else the images shared by all the games; created if missing
     */
    static File directory(final String folder) {
        return COLORING.equals(folder) ? GazePlayDirectories.getColoringDirectory()
            : GazePlayDirectories.getImagesDirectory();
    }

    /**
     * @return the images of a folder of the user, in the order of their names
     */
    static List<File> images(final String folder) {
        final File[] files = directory(folder).listFiles(file -> file.isFile() && isImage(file));
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files).sorted(Comparator.comparing(File::getName)).toList();
    }

    /**
     * @return whether the file is an image that can be given, by its extension
     */
    static boolean isImage(final File file) {
        final String name = file.getName();
        final int dot = name.lastIndexOf('.');
        return dot > 0 && IMAGE_EXTENSIONS.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
    }
}
