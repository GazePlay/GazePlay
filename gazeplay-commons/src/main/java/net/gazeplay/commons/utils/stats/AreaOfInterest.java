package net.gazeplay.commons.utils.stats;

import lombok.Data;

import java.util.List;

@Data
public class AreaOfInterest {
    private final String aoiID;
    private final List<CoordinatesTracker> points;
    private final int fixations;
    private final int centerX;
    private final int centerY;
    private final Double[] convexPoints;
    private final int startingIndex;
    private final int endingIndex;
    private final double timeSpent;
    private final double ttff;

    private double priority;

    public AreaOfInterest(
        final String aoiID,
        final List<CoordinatesTracker> points,
        final int centerX,
        final int centerY,
        final Double[] convexPoints,
        final int startingIndex,
        final int endingIndex,
        final double timeSpent,
        final double ttff
    ) {
        this.aoiID = aoiID;
        this.points = points;
        this.fixations = points.size();
        this.centerX = centerX;
        this.centerY = centerY;
        this.convexPoints = convexPoints;
        this.startingIndex = startingIndex;
        this.endingIndex = endingIndex;
        this.timeSpent = timeSpent;
        this.ttff = ttff;
    }

    public long getTimeStarted() {
        return points.get(0).getTimeStarted();
    }

    public long getTimeEnded() {
        return points.get(points.size() - 1).getTimeStarted() + points.get(points.size() - 1).getIntervalTime();
    }
}
