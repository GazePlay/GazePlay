package net.gazeplay.commons.utils.stats;

import lombok.Data;

@Data
public class AreaOfInterest {
    private final String id;
    private final int centerX;
    private final int centerY;
    private final Double[] pts;
    private final int startIdx;
    private final int endIdx;
    private final double time;
    private final double ttff;
    private double priority;

    public AreaOfInterest(
        final String id,
        final int centerX,
        final int centerY,
        final Double[] pts,
        final int startIdx,
        final int endIdx,
        final double time,
        final double ttff
    ) {
        this.id = id;
        this.centerX = centerX;
        this.centerY = centerY;
        this.pts = pts;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.time = time;
        this.ttff = ttff;
    }

    public int getFixations() {
        return endIdx - startIdx + 1;
    }
}
