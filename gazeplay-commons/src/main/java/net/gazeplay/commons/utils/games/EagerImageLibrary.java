package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

/**
 * This class implies that all Images are loaded in memory at all time. So it is only appropriate when the number of
 * items is limited.
 * <p>
 * For that reason, the constructor accepting a directory File is only present for compatibility reason, and should
 * generally not be used.
 * <p>
 * Please consider using LazyImageLibrary instead, when the number of item is unknown or may be high.
 * <p>
 * EagerImageLibrary exists essentially as an opposite of LazyImageLibrary and should generally not be used.
 */
@Slf4j
public class EagerImageLibrary extends AbstractImageLibrary {

    private final List<Image> allImages;

    @Deprecated
    public EagerImageLibrary(final File directoryFile) {
        this(ImageUtils.loadAllImages(directoryFile), null);
    }

    @Deprecated
    public EagerImageLibrary(final File directoryFile, final ImageLibrary defaultImageLibrary) {
        this(ImageUtils.loadAllImages(directoryFile), defaultImageLibrary);
    }

    public EagerImageLibrary(final List<Image> allImages) {
        this.allImages = allImages;
        setFallbackImageLibrary(null);
    }

    public EagerImageLibrary(final List<Image> allImages, final ImageLibrary defaultImageLibrary) {
        this.allImages = allImages;
        setFallbackImageLibrary(defaultImageLibrary);
    }

    @Override
    public int getImagesCount() {
        return allImages.size();
    }

    @Override
    public Image loadImageAtIndex(final int index) {
        return allImages.get(index);
    }

}
