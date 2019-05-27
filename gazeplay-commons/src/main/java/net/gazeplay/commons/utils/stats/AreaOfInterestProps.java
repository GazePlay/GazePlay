package net.gazeplay.commons.utils.stats;

import javafx.geometry.Point2D;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.util.ArrayList;

public class AreaOfInterestProps {
    private final long TTFF;
    private final ArrayList<CoordinatesTracker> listOfPoints;
    // private final int revisits;
    private final long timeSpent;
    private final int fixations;
    private final int centerX;
    private final int centerY;
    private final Double[] convexPoints;
    private final Point2D[] allPoint2DOfConvex;
    private final int startingIndex;
    private final int endingIndex;
    private final javafx.scene.shape.Polygon areaOfInterest;
    private final InfoBoxProps infoBox;

    public AreaOfInterestProps(long TTFF, long timeSpent, ArrayList<CoordinatesTracker> listOfPoints, int centerX,
            int centerY, Double[] convexPoints, Point2D[] allPoint2DOfConvex, int startingIndex, int endingIndex,
            javafx.scene.shape.Polygon areaOfInterest, InfoBoxProps infoBox) {
        this.TTFF = TTFF;
        this.listOfPoints = listOfPoints;
        this.timeSpent = timeSpent;
        this.fixations = listOfPoints.size();
        this.centerX = centerX;
        this.centerY = centerY;
        this.convexPoints = convexPoints;
        this.allPoint2DOfConvex = allPoint2DOfConvex;
        this.startingIndex = startingIndex;
        this.endingIndex = endingIndex;
        this.areaOfInterest = areaOfInterest;
        this.infoBox = infoBox;
    }

    public Double[] getConvexPoints() {
        return this.convexPoints;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public int getCenterY() {
        return this.centerY;
    }

    public int getFixations() {
        return this.fixations;
    }

    public long getTimeSpent() {
        return this.timeSpent;
    }

    public long getTTFF() {
        return this.TTFF;
    }

    public Point2D[] getAllPoint2DOfConvex() {
        return this.allPoint2DOfConvex;
    }

    public int getStartingIndex() {
        return this.startingIndex;
    }

    public int getEndingIndex() {
        return this.endingIndex;
    }

    public javafx.scene.shape.Polygon getAreaOfInterest() {
        return this.areaOfInterest;
    }

    public InfoBoxProps getInfoBox() {
        return this.infoBox;
    }

}
