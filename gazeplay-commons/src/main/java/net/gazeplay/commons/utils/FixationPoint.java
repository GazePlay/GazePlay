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

    public FixationPoint() {
        firstGaze = 0;
        gazeDuration = 0;
    }

    public FixationPoint(int fG, int gD) {
        firstGaze = fG;
        gazeDuration = gD;
    }
}
