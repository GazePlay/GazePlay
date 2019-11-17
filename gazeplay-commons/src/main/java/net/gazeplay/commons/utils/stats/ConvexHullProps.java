package net.gazeplay.commons.utils.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConvexHullProps {
    private final int centerX;
    private final int centerY;
    private final javafx.scene.shape.Polygon convexHull;
    private final Double[] convexPoints;
}
