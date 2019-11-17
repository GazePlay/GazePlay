package net.gazeplay.commons.utils.stats;

import javafx.geometry.Point2D;
import lombok.Data;

import java.util.List;

@Data
public class AreaOfInterestProps {
    private final List<CoordinatesTracker> listOfPoints;
    private final int fixations;
    private final int centerX;
    private final int centerY;
    private final Double[] convexPoints;
    private final Point2D[] allPoint2DOfConvex;
    private final int startingIndex;
    private final int endingIndex;
    private final javafx.scene.shape.Polygon areaOfInterest;
    private final InfoBoxProps infoBoxProps;
    private final long areaStartTime;
    private final long areaEndTime;

    private double priority;

    public AreaOfInterestProps(
        List<CoordinatesTracker> listOfPoints,
        int centerX, int centerY,
        Double[] convexPoints, Point2D[] allPoint2DOfConvex,
        int startingIndex, int endingIndex,
        javafx.scene.shape.Polygon areaOfInterest, InfoBoxProps infoBoxProps,
        long areaStartTime, long areaEndTime
    ) {
        this.listOfPoints = listOfPoints;
        this.fixations = listOfPoints.size();
        this.centerX = centerX;
        this.centerY = centerY;
        this.convexPoints = convexPoints;
        this.allPoint2DOfConvex = allPoint2DOfConvex;
        this.startingIndex = startingIndex;
        this.endingIndex = endingIndex;
        this.areaOfInterest = areaOfInterest;
        this.infoBoxProps = infoBoxProps;
        this.areaStartTime = areaStartTime;
        this.areaEndTime = areaEndTime;
    }

}
