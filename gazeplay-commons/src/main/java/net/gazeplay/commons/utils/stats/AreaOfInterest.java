package net.gazeplay.commons.utils.stats;

import lombok.Data;

import java.util.List;

@Data
public class AreaOfInterest {
    private final String ID;
    private final List<CoordinatesTracker> points;
    private final int fixations;
    private final int centerX;
    private final int centerY;
    private final Double[] convexPoints;
    private final int startingIndex;
    private final int endingIndex;
    private final long areaStartTime;
    private final long areaEndTime;
    private final double timeSpent;
    private final double TTFF;

    private double priority;

    public AreaOfInterest(
        final String aoiID,
        final List<CoordinatesTracker> points,
        final int centerX,
        final int centerY,
        final Double[] convexPoints,
        final int startingIndex,
        final int endingIndex,
        final long areaStartTime,
        final long areaEndTime,
        final double timeSpent,
        final double TTFF
    ) {
        this.ID = aoiID;
        this.points = points;
        this.fixations = points.size();
        this.centerX = centerX;
        this.centerY = centerY;
        this.convexPoints = convexPoints;
        this.startingIndex = startingIndex;
        this.endingIndex = endingIndex;
        this.areaStartTime = areaStartTime;
        this.areaEndTime = areaEndTime;
        this.timeSpent = timeSpent;
        this.TTFF = TTFF;
    }
}
