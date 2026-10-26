package net.gazeplay.components;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import lombok.Getter;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;

/**
 * A circle filled with an image drawn from a library, at a random position (the bubbles of the "Bubbles" game, for
 * instance).
 * <p>
 * Created by schwab on 12/08/2016.
 */
public class Portrait extends Circle {

    /**
     * @return the library of the "portraits" folder of the images of the user, with the default images as fallback
     */
    public static ImageLibrary createImageLibrary(ReplayablePseudoRandom randomGenerator) {
        return ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("portraits"), randomGenerator);
    }

    @Getter
    private final int initialRadius;

    public Portrait(final int initialRadius, final RandomPositionGenerator randomPositionGenerator, final ImageLibrary imageLibrary) {
        super(initialRadius);
        this.initialRadius = initialRadius;

        this.setPosition(randomPositionGenerator.newRandomPosition(initialRadius));

        setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
    }

    /**
     * Moves the center of the circle.
     */
    public void setPosition(final Position position) {
        this.setCenterX(position.getX());
        this.setCenterY(position.getY());
    }

}
