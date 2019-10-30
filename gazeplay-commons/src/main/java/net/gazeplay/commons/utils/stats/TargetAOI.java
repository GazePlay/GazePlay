package net.gazeplay.commons.utils.stats;

import javafx.scene.shape.Polygon;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TargetAOI {
    private final double xValue;
    private final double yValue;
    private final int areaRadius;
    private final long timeStarted;

    private long duration;
    private Polygon polygon;
}
