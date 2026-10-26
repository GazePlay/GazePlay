package net.gazeplay.commons.utils.stats;

import javafx.scene.shape.Polygon;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A target of a game, given to the statistics for the areas of interest screen: its position, its radius, when it
 * appeared and disappeared, and its outline.
 */
@Data
@RequiredArgsConstructor
public class TargetAOI {
    private final double xValue;
    private final double yValue;
    private final int areaRadius;
    private final long timeStarted;

    @Setter
    private long timeEnded;
    private long duration;
    private Polygon polygon;
}
