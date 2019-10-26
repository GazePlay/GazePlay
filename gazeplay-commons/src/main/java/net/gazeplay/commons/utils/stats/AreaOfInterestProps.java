package net.gazeplay.commons.utils.stats;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class AreaOfInterestProps {
    private final ArrayList<CoordinatesTracker> listOfPoints;
    private final int fixations;
    private final int centerX;
    private final int centerY;
    private final Double[] convexPoints;
    private final Point2D[] allPoint2DOfConvex;
    private final int startingIndex;
    private final int endingIndex;
    private final javafx.scene.shape.Polygon areaOfInterest;
    private final InfoBoxProps infoBox;
    private double priority;
    private final long areaStartTime;
    private final long areaEndTime;

    public AreaOfInterestProps(ArrayList<CoordinatesTracker> listOfPoints, int centerX, int centerY,
            Double[] convexPoints, Point2D[] allPoint2DOfConvex, int startingIndex, int endingIndex,
            javafx.scene.shape.Polygon areaOfInterest, InfoBoxProps infoBox, long areaStartTime, long areaEndTime) {
        this.listOfPoints = listOfPoints;
        this.fixations = listOfPoints.size();
        this.centerX = centerX;
        this.centerY = centerY;
        this.convexPoints = convexPoints;
        this.allPoint2DOfConvex = allPoint2DOfConvex;
        this.startingIndex = startingIndex;
        this.endingIndex = endingIndex;
        this.areaOfInterest = areaOfInterest;
        this.infoBox = infoBox;
        this.areaStartTime = areaStartTime;
        this.areaEndTime = areaEndTime;
    }

    public Double[] getConvexPoints() {
        return this.convexPoints;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public double getPriority() {
        return this.priority;
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

    public InfoBoxProps getInfoBoxProp() {
        return this.infoBox;
    }

    public long getAreaStartTime() {
        return this.areaStartTime;
    }

    public long getAreaEndTime() {
        return this.areaEndTime;
    }

}
