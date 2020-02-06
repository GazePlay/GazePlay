package net.gazeplay.commons.utils;

import lombok.Getter;
import lombok.Setter;

public class FixationPoint {

    @Setter
    @Getter
    private long timeGaze;
    @Setter
    @Getter
    private long gazeDuration;
    @Setter
    @Getter
    private int x;
    @Setter
    @Getter
    private int y;

    public FixationPoint() {
        timeGaze = 0;
        gazeDuration = 0;
        x = -1;
        y = -1;
    }

    public FixationPoint(long timeGaze, long gazeDuration, int X, int Y) {
        this.timeGaze = timeGaze;
        this.gazeDuration = gazeDuration;
        x = X;
        y = Y;
    }

    @Override
    public String toString() {
        return "FixationPoint{" + "timeGaze=" + timeGaze + ", gazeDuration=" + gazeDuration + ", x=" + x + ", y=" + y
            + '}';
    }
}
