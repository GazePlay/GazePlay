package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class CustomButton extends Rectangle {

    public CustomButton(String imageResourceLocation, Dimension2D screenDimension) {
        this(imageResourceLocation, screenDimension.getWidth() / 20d);

        ImagePattern value = new ImagePattern(new Image(imageResourceLocation), 0, 0, 1, 1, true);
        this.setFill(value);
    }

    public CustomButton(String imageResourceLocation, double size) {
        super(0, 0, size, size);

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
