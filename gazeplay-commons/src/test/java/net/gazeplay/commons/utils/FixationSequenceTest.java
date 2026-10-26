package net.gazeplay.commons.utils;

import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
class FixationSequenceTest {

    private ArrayList<LinkedList<FixationPoint>> fixationPoints =
        new ArrayList<>( List.of (
            new LinkedList<>(
                List.of(
                new FixationPoint(123, 200, 20, 40),
                new FixationPoint(123, 200, 500, 400),
                new FixationPoint(123, 200, 120, 150),
                new FixationPoint(123, 50, 300, 450),
                new FixationPoint(123, 50, 300, 400),
                new FixationPoint(123, 50, 300, 400),
                new FixationPoint(123, 50, 300, 400)
                )
            ),
            new LinkedList<>(
                List.of(
                    new FixationPoint(123, 200, 20, 40),
                    new FixationPoint(123, 200, 500, 400),
                    new FixationPoint(123, 200, 120, 150),
                    new FixationPoint(123, 50, 300, 450),
                    new FixationPoint(123, 50, 300, 400),
                    new FixationPoint(123, 50, 300, 400),
                    new FixationPoint(123, 50, 300, 400)
                )
            )
    ));

    /**
     * Runs on the JavaFX thread and reports the failures (the assertions used to run inside Platform.runLater, where a
     * failure is not reported).
     */
    private static <T> T onFxThread(java.util.function.Supplier<T> action) {
        java.util.concurrent.CompletableFuture<T> result = new java.util.concurrent.CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(action.get());
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
        });
        return result.join();
    }

    /**
     * The reduction keeps 5 points (only the points at 15 px or less from the last kept point are merged), then the
     * points of 100 ms or less are not drawn and removed: 4 remain. The last point used to be ignored by the reduction,
     * so (300, 400) only got 100 ms and was removed too: there were 3 points.
     */
    @Test
    void shouldCreateFixationSequence() {
        for (int index : List.of(FixationSequence.MOUSE_FIXATION_SEQUENCE, FixationSequence.GAZE_FIXATION_SEQUENCE)) {
            FixationSequence sequence = onFxThread(() -> new FixationSequence(1920, 1080, fixationPoints, index));
            assertEquals(4, sequence.getSequence().size());
            assertEquals(1080, sequence.getImage().heightProperty().get());
            assertEquals(1920, sequence.getImage().widthProperty().get());
        }
    }

    @Test
    void shouldSaveImageToFile() {
        for (int index : List.of(FixationSequence.MOUSE_FIXATION_SEQUENCE, FixationSequence.GAZE_FIXATION_SEQUENCE)) {
            File testFile = new File("image.png");
            onFxThread(() -> {
                new FixationSequence(1920, 1080, fixationPoints, index).saveToFile(testFile);
                return null;
            });

            assertTrue(testFile.isFile());
            assertTrue(testFile.delete());
        }
    }

    /**
     * The points closer than the tolerance to the last kept point are merged into it, their durations added. The
     * last point of the sequence used to be ignored, merged or not.
     */
    @Test
    void shouldReduceTheVerticesUpToTheLastPoint() {
        LinkedList<FixationPoint> points = new LinkedList<>(List.of(
            new FixationPoint(0, 100, 10, 10),
            new FixationPoint(100, 200, 15, 12),
            new FixationPoint(300, 50, 400, 400),
            new FixationPoint(350, 70, 405, 401)));

        LinkedList<FixationPoint> reduced = FixationSequence.vertexReduction(points, 15);

        assertEquals(2, reduced.size());
        assertEquals(300, reduced.get(0).getGazeDuration());
        assertEquals(120, reduced.get(1).getGazeDuration());
    }

    @Test
    void shouldKeepADistantLastPoint() {
        LinkedList<FixationPoint> points = new LinkedList<>(List.of(
            new FixationPoint(0, 100, 10, 10),
            new FixationPoint(100, 200, 500, 500)));

        assertEquals(2, FixationSequence.vertexReduction(points, 15).size());
    }
}
