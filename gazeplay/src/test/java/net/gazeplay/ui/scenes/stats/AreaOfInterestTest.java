package net.gazeplay.ui.scenes.stats;

import javafx.geometry.Point2D;
import net.gazeplay.commons.utils.stats.TargetAOI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.ApplicationExtension;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
class AreaOfInterestTest {

    @Test
    void shouldCalculateRectangle() {
        Point2D[] input = new Point2D[]{
            new Point2D(706.0, 685.0),
            new Point2D(710.0, 670.0),
            new Point2D(708.0, 690.0)
        };

        Double[] expected = new Double[]{
            706.0 - 15,
            690.0 + 15,
            710.0 + 15,
            690.0 + 15,
            710.0 + 15,
            670.0 - 15,
            706.0 - 15,
            670.0 - 15
        };

        Double[] actual = AreaOfInterest.calculateRectangle(input);

        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldCalculateOrientation() {
        // Collinear
        assertEquals(0, AreaOfInterest.orientation(
            new Point2D(0, 0),
            new Point2D(1, 1),
            new Point2D(2, 2)
        ));

        // Clockwise
        assertEquals(1, AreaOfInterest.orientation(
            new Point2D(2, 5),
            new Point2D(1, 2),
            new Point2D(0, 0)
        ));

        // Counterclockwise
        assertEquals(-1, AreaOfInterest.orientation(
            new Point2D(0, 0),
            new Point2D(1, 2),
            new Point2D(2, 5)
        ));
    }

    @Test
    void shouldCalculateConvexHull() {
        Point2D[] input = new Point2D[]{
            new Point2D(2, 2),
            new Point2D(2, 3),
            new Point2D(3, 5),
            new Point2D(1, 2),
            new Point2D(1.25, 3),
            new Point2D(2, 1),
            new Point2D(4, 2)
        };

        Double[] expected = new Double[] {
          1d, 2d, 2d, 1d, 4d, 2d, 3d, 5d, 1.25, 3d
        };

        Double[] actual = AreaOfInterest.calculateConvexHull(input);

        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldCalculateTargetAOI() {
        TargetAOI t1 = new TargetAOI(500, 500, 300, 1234);
        TargetAOI t2 = new TargetAOI(650, 700, 200, 1234);

        Double[] e1 = new Double[] {
            385d, 615d, 815d, 615d, 815d, 185d, 385d, 185d
        };
        Double[] e2 = new Double[] {
            535d, 815d, 865d, 815d, 865d, 485d, 535d, 485d
        };

        ArrayList<TargetAOI> input = new ArrayList<>(List.of(t1, t2));

        AreaOfInterest.calculateTargetAOI(input);

        Double[] r1 = new Double[8], r2 = new Double[8];
        input.get(0).getPolygon().getPoints().toArray(r1);
        input.get(1).getPolygon().getPoints().toArray(r2);

        assertArrayEquals(r1, e1);
        assertArrayEquals(r2, e2);
    }
}
