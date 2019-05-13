package net.gazeplay.commons.utils;

import lombok.Getter;
import lombok.Setter;

public class FixationPoint {

    @Setter
    @Getter
    private long firstGaze;
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
        firstGaze = 0;
        gazeDuration = 0;
        x = -1;
        y = -1;
    }

    public FixationPoint(long fG, long gD, int X, int Y) {
        firstGaze = fG;
        gazeDuration = gD;
        x = X;
        y = Y;
    }
}
