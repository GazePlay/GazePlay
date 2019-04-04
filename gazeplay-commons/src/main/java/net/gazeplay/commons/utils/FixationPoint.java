package net.gazeplay.commons.utils;

import lombok.Getter;
import lombok.Setter;

public class FixationPoint {

    @Setter
    @Getter
    private int firstGaze;
    @Setter
    @Getter
    private int gazeDuration;

    public FixationPoint() {
        firstGaze = 0;
        gazeDuration = 0;
    }

    public FixationPoint(int fG, int gD) {
        firstGaze = fG;
        gazeDuration = gD;
    }
}
