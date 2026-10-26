package net.gazeplay.commons.utils.games;

import javafx.scene.image.Image;

import java.util.List;
import java.util.Set;

/**
 * A set of images a game draws its pictures from (e.g. the portraits of the bubbles), randomly.
 */
public interface ImageLibrary {

    /**
     * @return the number of images of the library itself (without its fallback)
     */
    int getImagesCount();

    /**
     * @return an image drawn at random
     */
    Image pickRandomImage();

    /**
     * @return {@code count} different images drawn at random
     * @throws IllegalArgumentException when the library and its fallback do not have enough images
     */
    Set<Image> pickMultipleRandomDistinctImages(int count);

    /**
     * @return all the images of the library itself, in order
     */
    List<Image> pickAllImages();

}
