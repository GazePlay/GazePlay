package net.gazeplay.commons.utils;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

public class CustomButton extends Rectangle {

    private static final double DEFAULT_SIZE;

    static {
        // DEFAULT_SIZE = 128;
        // DEFAULT_SIZE = Screen.getPrimary().getBounds().getWidth() / 10;
        DEFAULT_SIZE = Screen.getPrimary().getBounds().getWidth() / 20;
    }

    public CustomButton(String imageResourceLocation) {
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
