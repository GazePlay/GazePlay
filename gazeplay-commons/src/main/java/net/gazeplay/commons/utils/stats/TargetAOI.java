package net.gazeplay.commons.utils.stats;

import javafx.scene.shape.Polygon;

public class TargetAOI {
    private final double xValue;
    private final double yValue;
    private final int radius;
    private long duration;
    private final long timeStarted;
    private Polygon polygon;

    public TargetAOI(double xValue, double yValue, int radius, long timeStarted) {
        this.xValue = xValue;
        this.yValue = yValue;
        // this.intervalTime = intervalTime;
        this.timeStarted = timeStarted;
        this.radius = radius;
    }

    public double getxValue() {
        return this.xValue;
    }

    public double getyValue() {
        return this.yValue;
    }

    public int getAreaRadius() {
        return this.radius;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public Polygon getPolygon() {
        return this.polygon;
    }

    public long getTimeStarted() {
        return this.timeStarted;
    }

}
