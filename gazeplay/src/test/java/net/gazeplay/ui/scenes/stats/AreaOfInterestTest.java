package net.gazeplay.ui.scenes.stats;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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

        Double[] result = AreaOfInterest.calculateRectangle(input);

        assertArrayEquals(expected, result);
    }
}
