package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;

import java.util.Set;

public interface ImageLibrary {

    int getImagesCount();

    Image pickRandomImage();

    Set<Image> pickMultipleRandomDistinctImages(int count);

}
