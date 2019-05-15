package net.gazeplay.commons.utils.stats;

public class CoordinatesTracker {
    private final int xValue;
    private final int yValue;
    private final long timeValue;
    private final long intervalTime;

    public CoordinatesTracker(int xValue, int yValue, long timeValue,long intervalTime) {
        this.xValue = xValue;
        this.yValue = yValue;
        this.timeValue = timeValue;
        this.intervalTime = intervalTime;
    }

    public int getxValue()
    {
        return this.xValue;
    }
    public int getyValue()
    {
        return this.yValue;
    }
    public long getTimeValue(){ return this.timeValue; }
    public long getIntervalTime(){ return this.intervalTime;}
}
