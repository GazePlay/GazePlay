package net.gazeplay.commons.utils.stats;

import javafx.scene.shape.Polygon;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConvexHull {
    private final int centerX;
    private final int centerY;
    private final Polygon convexHull;
    private final Double[] convexPoints;
}
