package net.gazeplay.commons.utils.stats;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CoordinatesTracker {
    private final double x;
    private final double y;
    private final long start;
    private final long interval;
    private final int event;
    private double dist;

    public String getEvent() {
        return event == 0 ? "gaze" : "mouse";
    }
}
