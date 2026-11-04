package net.gazeplay.components;

import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ring of a fixation around a card, checked on a rendering: it starts at the top center and goes clockwise, like the
 * round rings (maintainer's remark of 4/11/2026: it went the other way, from the top left).
 */
@ExtendWith(ApplicationExtension.class)
class GameStyleRingTest {

    private static final double WIDTH = 300;
    private static final double HEIGHT = 400;

    private static WritableImage render(final double progress) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            final Rectangle ring = new Rectangle(0, 0, WIDTH, HEIGHT);
            ring.setArcWidth(60);
            ring.setArcHeight(60);
            ring.setFill(null);
            ring.setStroke(Color.RED);
            ring.setStrokeType(StrokeType.INSIDE);
            ring.setStrokeLineCap(StrokeLineCap.BUTT);
            ring.setStrokeWidth(12);
            final double perimeter = GameStyle.clockwiseRing(ring);
            GameStyle.fillRing(ring, perimeter, progress);
            final SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.WHITE);
            return new Group(ring).snapshot(parameters, null);
        }).get();
    }

    private static boolean filledAt(final WritableImage image, final double x, final double y) {
        return image.getPixelReader().getColor((int) x, (int) y).getGreen() < 0.5;
    }

    @Test
    void shouldStartAtTheTopCenterAndGoClockwise() throws Exception {
        final WritableImage quarter = render(0.25);
        final double w = quarter.getWidth();
        final double h = quarter.getHeight();
        assertTrue(filledAt(quarter, w * 0.7, 6), "top, right of the center");
        assertTrue(filledAt(quarter, w - 7, h * 0.3), "right side, high");
        assertFalse(filledAt(quarter, w * 0.3, 6), "top, left of the center");
        assertFalse(filledAt(quarter, 6, h * 0.3), "left side");

        final WritableImage half = render(0.5);
        assertTrue(filledAt(half, w * 0.7, h - 7), "bottom, right of the center");
        assertFalse(filledAt(half, w * 0.3, h - 7), "bottom, left of the center");
    }

    /**
     * The round rings drawn with a dashed circle turned by -90° (Sounds Of Life, GazePlay Eval) go the same way.
     */
    @Test
    void shouldGoTheSameWayAsADashedCircleTurnedToTheTop() throws Exception {
        final WritableImage quarter = WaitForAsyncUtils.asyncFx(() -> {
            final double radius = 150;
            final Circle ring = new Circle(radius, radius, radius);
            ring.setRotate(-90);
            ring.setFill(null);
            ring.setStroke(Color.RED);
            ring.setStrokeLineCap(StrokeLineCap.BUTT);
            ring.setStrokeWidth(12);
            final double perimeter = 2 * Math.PI * radius;
            ring.getStrokeDashArray().setAll(0.25 * perimeter, perimeter);
            final SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.WHITE);
            return new Group(ring).snapshot(parameters, null);
        }).get();
        final double size = quarter.getWidth();
        final double middle = 150 * Math.sqrt(0.5);
        assertTrue(filledAt(quarter, size / 2 + middle, size / 2 - middle), "top right");
        assertFalse(filledAt(quarter, size / 2 - middle, size / 2 - middle), "top left");
        assertFalse(filledAt(quarter, size / 2 + middle, size / 2 + middle), "bottom right");
    }

    @Test
    void shouldBeWholeAtTheEndOfTheFixation() throws Exception {
        final WritableImage whole = render(1);
        final double w = whole.getWidth();
        final double h = whole.getHeight();
        assertTrue(filledAt(whole, w * 0.3, 6));
        assertTrue(filledAt(whole, 6, h * 0.3));
        assertTrue(filledAt(whole, w * 0.7, h - 7));
    }

    @Test
    void shouldGiveTheLengthOfTheOutline() {
        final Rectangle ring = new Rectangle(0, 0, WIDTH, HEIGHT);
        ring.setArcWidth(60);
        ring.setArcHeight(60);
        assertEquals(2 * (WIDTH + HEIGHT) - (8 - 2 * Math.PI) * 30, GameStyle.clockwiseRing(ring), 1e-9);
    }
}
