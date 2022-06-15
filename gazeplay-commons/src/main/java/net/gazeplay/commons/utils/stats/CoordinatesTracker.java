package net.gazeplay.commons.utils.stats;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CoordinatesTracker {
    private final int xValue;
    private final int yValue;
    private final long intervalTime;
    private final long timeStarted;
    private final String event;
    private double distance;
}
