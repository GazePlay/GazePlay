package net.gazeplay.commons.utils.stats;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A sample kept for the areas of interest: its position in the scene, the time elapsed since the previous kept sample
 * ({@code intervalTime}), and the time it was taken ({@code timeStarted}, System.currentTimeMillis()). {@code distance}
 * is a working value of the drawing of the areas.
 */
@Data
@RequiredArgsConstructor
public class CoordinatesTracker {
    private final int xValue;
    private final int yValue;
    private final long intervalTime;
    private final long timeStarted;
    private double distance;
}
