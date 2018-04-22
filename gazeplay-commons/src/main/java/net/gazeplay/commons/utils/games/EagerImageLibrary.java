package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@Slf4j
public class EagerImageLibrary extends AbstractImageLibrary {

    private final List<Image> allImages;

    public EagerImageLibrary(File directoryFile) {
        this(ImageUtils.loadAllImagesInDirectory(directoryFile), null);
    }

    public EagerImageLibrary(File directoryFile, ImageLibrary defaultImageLibrary) {
        this(ImageUtils.loadAllImagesInDirectory(directoryFile), defaultImageLibrary);
    }

    public EagerImageLibrary(List<Image> allImages) {
        this.allImages = allImages;
        setFallbackImageLibrary(null);
    }

    public EagerImageLibrary(List<Image> allImages, ImageLibrary defaultImageLibrary) {
        this.allImages = allImages;
        setFallbackImageLibrary(defaultImageLibrary);
    }

    @Override
    public int getImagesCount() {
        return allImages.size();
    }

    public Image loadImageAtIndex(int index) {
        return allImages.get(index);
    }

}
