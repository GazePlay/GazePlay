package net.gazeplay.utils;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import utils.games.Utils;

/**
 * Created by schwab on 12/08/2016.
 */
public class Portrait extends Circle {

    // protected static final int maxX = 1000;
    // protected static final int maxY = 500;

    protected int radius = initRadius;

    protected static final int initRadius = 100;

    protected static final double maxX = Screen.getPrimary().getBounds().getMaxX() - 2 * initRadius;
    protected static final double maxY = Screen.getPrimary().getBounds().getMaxY() - 2 * initRadius;

    private static Image[] photos;

    public Portrait(int radius) {

        super(radius);
        this.setPosition(newX(), newY());
        this.radius = radius;
        photos = Utils.images(Utils.getImagesFolder() + "portraits");
        setFill(new ImagePattern(newPhoto(), 0, 0, 1, 1, true));
    }

    public void setPosition(double X, double Y) {

        this.setCenterX(X);
        this.setCenterY(Y);
    }

    protected Image newPhoto() {

        return photos[((int) (photos.length * Math.random()))];

    }

    protected int newX() {

        return (int) (Math.random() * maxX) + radius;
    }

    protected int newY() {

        return (int) (Math.random() * maxY * 2 / 3) + radius;
    }
}