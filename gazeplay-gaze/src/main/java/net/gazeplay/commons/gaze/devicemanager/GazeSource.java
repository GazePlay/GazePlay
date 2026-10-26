package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An eye tracker, seen by GazePlay: it gives gaze samples, in screen coordinates, from any thread. The dispatch to the
 * nodes of the games (on the JavaFX thread) is done by {@code GazeSourceDeviceManager}.
 */
public interface GazeSource {

    /**
     * No eye tracker (mouse_control): no sample.
     */
    GazeSource NONE = new GazeSource() {
        @Override
        public void start(Supplier<Dimension2D> screenDimension, Supplier<Point2D> screenPosition, Consumer<GazeSample> consumer) {
        }

        @Override
        public void stop() {
        }
    };

    /**
     * @param screenDimension dimension of the screen showing GazePlay
     * @param screenPosition  position of that screen in the desktop (second screen)
     * @param consumer        receives the samples, from any thread
     */
    void start(Supplier<Dimension2D> screenDimension, Supplier<Point2D> screenPosition, Consumer<GazeSample> consumer);

    /**
     * Stops giving samples. The source can be started again.
     */
    void stop();
}
