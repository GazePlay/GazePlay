package net.gazeplay.commons.utils;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * Created by schwab on 22/12/2016.
 */
public class HomeButton extends Rectangle {

    private static final int DEFAULT_SIZE = 100;

    private final String imageResourceLocation = "data/common/images/home-button.png";

    public HomeButton() {
        super(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);

        ImagePattern value = new ImagePattern(new Image(imageResourceLocation), 0, 0, 1, 1, true);
        this.setFill(value);
    }

    public void recomputeSizeAndPosition(Scene scene) {
        double size = scene.getWidth() / 10;

        double positionX = scene.getWidth() - (size * 1.5);
        double positionY = scene.getHeight() - (size * 1.5);

        setX(positionX);
        setY(positionY);
        setWidth(size);
        setHeight(size);
    }

}
