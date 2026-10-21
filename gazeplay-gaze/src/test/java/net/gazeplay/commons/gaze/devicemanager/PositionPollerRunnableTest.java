package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionPollerRunnableTest {

    private final List<GazeSample> samples = new ArrayList<>();

    private PositionPollerRunnable pollerReturning(float[]... positions) {
        Deque<float[]> queue = new ArrayDeque<>(List.of(positions));
        return new PositionPollerRunnable(
            () -> new Dimension2D(1920, 1080),
            // Second screen at the right of the primary one
            () -> new Point2D(1920, 0),
            samples::add,
            queue::poll);
    }

    /**
     * Each poll gives a gaze sample to the consumer (the gaze device manager), with the time of the acquisition; the
     * value without gaze data gives nothing.
     */
    @Test
    void shouldGiveTheGazeSamplesToTheConsumer() {
        PositionPollerRunnable poller = pollerReturning(new float[]{0.5f, 0.5f}, new float[]{0.25f, 0.75f});
        long before = System.currentTimeMillis();

        poller.poll();
        poller.poll();

        assertEquals(1, samples.size());
        assertEquals(new Point2D(1920 + 480, 810), samples.get(0).screenPosition());
        assertEquals(GazeSample.Source.GAZE, samples.get(0).source());
        assertTrue(samples.get(0).time() >= before);
    }

    @Test
    void shouldConvertTheRatiosIntoAPositionOnTheScreen() {
        PositionPollerRunnable poller = pollerReturning(new float[]{0.25f, 0.75f});

        assertEquals(Optional.of(new Point2D(1920 + 480, 810)), poller.nextGazePoint());
    }

    /**
     * Regression test for R9: without eye tracker, the Tobii wrapper returns (0.5, 0.5) at each call, which used to be
     * sent to the games (and recorded in the results) as a gaze fixed at the center of the screen.
     */
    @Test
    void shouldIgnoreTheValueReturnedWithoutGazeData() {
        PositionPollerRunnable poller = pollerReturning(
            new float[]{0.5f, 0.5f}, new float[]{0.5f, 0.5f}, new float[]{0.5f, 0.25f});

        assertEquals(Optional.empty(), poller.nextGazePoint());
        assertEquals(Optional.empty(), poller.nextGazePoint());
        // Only the exact pair is ignored
        assertEquals(Optional.of(new Point2D(1920 + 960, 270)), poller.nextGazePoint());
    }
}
