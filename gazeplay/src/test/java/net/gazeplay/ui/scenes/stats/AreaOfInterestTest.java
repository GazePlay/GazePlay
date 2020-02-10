package net.gazeplay.ui.scenes.stats;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.ApplicationExtension;

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
}
