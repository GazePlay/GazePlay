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

    public HomeButton() {
        super(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);

        this.setFill(new ImagePattern(new Image("data/common/images/home-button.png"), 0, 0, 1, 1, true));
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
