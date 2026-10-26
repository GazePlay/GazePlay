package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * A square button showing an image (home, exit, continue...).
 */
public class CustomButton extends Rectangle {

    /**
     * A button whose side is a twentieth of the width of the screen.
     */
    public CustomButton(String imageResourceLocation, Dimension2D screenDimension) {
        this(imageResourceLocation, screenDimension.getWidth() / 20d);

        ImagePattern value = new ImagePattern(new Image(imageResourceLocation), 0, 0, 1, 1, true);
        this.setFill(value);
    }

    /**
     * A button of the given side, in pixels.
     */
    public CustomButton(String imageResourceLocation, double size) {
        super(0, 0, size, size);

        ImagePattern value = new ImagePattern(new Image(imageResourceLocation), 0, 0, 1, 1, true);
        this.setFill(value);
    }

}
