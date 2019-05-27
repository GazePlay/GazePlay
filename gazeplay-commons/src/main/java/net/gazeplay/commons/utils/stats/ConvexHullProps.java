package net.gazeplay.commons.utils.stats;

import java.awt.*;

public class ConvexHullProps {
    private final int centerX;
    private final int centerY;
    private final javafx.scene.shape.Polygon convexHull;
    private final Double[] convexPoints;

    public ConvexHullProps(int centerX, int centerY, javafx.scene.shape.Polygon convexHull, Double[] convexPoints) {

        this.centerX = centerX;
        this.centerY = centerY;
        this.convexHull = convexHull;
        this.convexPoints = convexPoints;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public int getCenterY() {
        return this.centerY;
    }

    public javafx.scene.shape.Polygon getConvexHull() {
        return this.convexHull;
    }

    public Double[] getConvextPoint() {
        return this.convexPoints;
    }

}
