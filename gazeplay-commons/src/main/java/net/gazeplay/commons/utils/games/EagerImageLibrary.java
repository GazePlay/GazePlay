package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;

import java.io.File;
import java.util.List;
import java.util.Random;

public class EagerImageLibrary implements ImageLibrary {

    private final List<Image> allImages;

    private final Random random = new Random();

    public EagerImageLibrary(File directoryFile) {
        this(ImageUtils.loadAllImagesInDirectory(directoryFile));
    }

    public EagerImageLibrary(List<Image> allImages) {
        this.allImages = allImages;
    }

    @Override
    public Image pickRandomImage() {
        final int randomIndex = random.nextInt(allImages.size());
        return allImages.get(randomIndex);
    }

}
