package net.gazeplay.commons.utils.stats;

public class CoordinatesTracker {
    private final int xValue;
    private final int yValue;
    private final long intervalTime;
    private final long timeStarted;
    private double distance;

    public CoordinatesTracker(int xValue, int yValue, long intervalTime, long timeStarted) {
        this.xValue = xValue;
        this.yValue = yValue;
        this.intervalTime = intervalTime;
        this.timeStarted = timeStarted;

    }

    public int getxValue() {
        return this.xValue;
    }

    public int getyValue() {
        return this.yValue;
    }

    public long getIntervalTime() {
        return this.intervalTime;
    }

    public long getTimeStarted() {
        return this.timeStarted;
    }

    public void setDistance(double distance){this.distance = distance;}

    public double getDistance(){ return this.distance;}

}
