package net.gazeplay.commons.utils.stats;

import java.awt.*;

public class ConvexHullProps {
    private final int centerX;
    private final int centerY;
    private final javafx.scene.shape.Polygon convexHull;
    public ConvexHullProps(int centerX, int centerY, javafx.scene.shape.Polygon convexHull)
    {

        this.centerX = centerX;
        this.centerY = centerY;
        this.convexHull = convexHull;
    }
    public int getCenterX(){
        return this.centerX;
    }
    public int getCenterY(){
        return this.centerY;
    }
    public javafx.scene.shape.Polygon getConvexHull(){
        return this.convexHull;
    }
}
