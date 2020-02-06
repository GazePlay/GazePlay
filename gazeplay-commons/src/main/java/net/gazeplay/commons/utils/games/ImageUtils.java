package net.gazeplay.commons.utils.games;

import com.google.common.collect.Sets;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

@Slf4j
public class ImageUtils {

    static final Set<String> supportedFilesExtensions = Collections
        .unmodifiableSet(Sets.newHashSet("jpg", "jpeg", "png", "gif", "bmp", "wbmp"));

    public static ImageLibrary createImageLibrary(final File directoryFile) {
        return new LazyImageLibrary(directoryFile, createDefaultImageLibrary(null));
    }

    public static ImageLibrary createImageLibrary(final File directoryFile, final File defaultDirectoryFile) {
        return new LazyImageLibrary(directoryFile,
            createDefaultImageLibrary(new LazyImageLibrary(defaultDirectoryFile)));
    }

    /**
     * Creates an Image Library that reads from the data/common/default/images directory
     * in the resources folder of the module from which it has been invoked.
     *
     * @param fallbackImageLibrary The library to use if, for whatever reason, the resources folder cannot be accessed.
     * @return A ResourceImageLibrary including the <pre>fallbackImageLibrary</pre>
     * @see net.gazeplay.commons.utils.games.ResourceImageLibrary
     */
    public static ImageLibrary createDefaultImageLibrary(final ImageLibrary fallbackImageLibrary) {
        final String defaultResourceDirectory = "data/common/default/images/";

        return new ResourceImageLibrary(defaultResourceDirectory, fallbackImageLibrary);
    }

    /**
     * Creates an Image Library that reads from data/ + your chosen path
     * in the resources folder of the module from which it has been invoked.
     *
     * @param fallbackImageLibrary The library to use if, for whatever reason, the resources folder cannot be accessed.
     * @param path                 The path within the resources folder to load your library from. For example, if you wanted to read from
     *                             data/colors/images, you would set this value to "colors/images".
     * @return A ResourceImageLibrary including the <pre>fallbackImageLibrary</pre>
     * @see net.gazeplay.commons.utils.games.ResourceImageLibrary
     */
    public static ImageLibrary createCustomizedImageLibrary(final ImageLibrary fallbackImageLibrary, final String path) {
        final String defaultResourceDirectory = "data/" + path;

        return new ResourceImageLibrary(defaultResourceDirectory, fallbackImageLibrary);
    }

    @Deprecated
    public static List<Image> loadAllImages(final File directoryFile) {
        final List<File> files = listImageFiles(directoryFile);

        return loadAllAsImages(files);
    }

    public static List<File> listImageFiles(final File directoryFile) {
        log.debug("Listing images in directory {}", directoryFile.getAbsolutePath());
        File[] files = directoryFile.listFiles(ImageUtils::isImage);
        if (files == null) {
            files = new File[0];
        }
        final List<File> result = Arrays.asList(files);
        log.debug("Found {} files in directory {}", files.length, directoryFile.getAbsolutePath());
        return result;
    }

    public static List<Image> loadAllAsImages(final List<File> imageFiles) {
        final List<Image> result = new ArrayList<>();
        for (final File currentFile : imageFiles) {
            result.add(loadImage(currentFile));
        }
        return result;
    }

    public static Image loadImage(final File file) {
        return new Image(file.toURI().toString());
    }

    public static Image loadImage(final String resource) {
        return new Image(resource);
    }

    private static boolean isImage(final File file) {
        if (file.isDirectory()) {
            return false;
        }
        if (!file.isFile()) {
            return false;
        }
        if (file.isHidden()) {
            return false;
        }

        final String filename = file.getName();
        if (filename.startsWith(".")) {
            // Problems with filenames starting with a dot on Windows
            return false;
        }
        final String extension = filename.substring(filename.lastIndexOf('.') + 1);
        final String extensionAsLowercase = extension.toLowerCase(Locale.getDefault());
        return supportedFilesExtensions.contains(extensionAsLowercase);
    }

}
