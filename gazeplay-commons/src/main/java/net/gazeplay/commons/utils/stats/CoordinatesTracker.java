package net.gazeplay.commons.utils.stats;

public class CoordinatesTracker {
    private final int xValue;
    private final int yValue;
    private final long timeToFixation;
    private final long intervalTime;
    private final long timeStarted;

    public CoordinatesTracker(int xValue, int yValue, long timeToFixation, long intervalTime, long timeStarted) {
        this.xValue = xValue;
        this.yValue = yValue;
        this.timeToFixation = timeToFixation;
        this.intervalTime = intervalTime;
        this.timeStarted = timeStarted;

    }

    public int getxValue()
    {
        return this.xValue;
    }
    public int getyValue()
    {
        return this.yValue;
    }
    public long getTimeToFixation(){ return this.timeToFixation; }
    public long getIntervalTime(){ return this.intervalTime;}
    public long getTimeStarted(){return this.timeStarted;}
}
