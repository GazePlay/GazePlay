package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;

import java.util.function.Supplier;

/**
 * Gaze device manager of a {@link GazeSource}: the samples of the source are dispatched to the nodes of the games.
 * Replaces the former TobiiGazeDeviceManager and EyeTribeGazeDeviceManager, which each mixed the acquisition of the
 * positions with their dispatch.
 */
public final class GazeSourceDeviceManager extends AbstractGazeDeviceManager {

    private final GazeSource source;

    public GazeSourceDeviceManager(final GazeSource source) {
        this.source = source;
    }

    @Override
    public void init(final Supplier<Dimension2D> currentScreenDimensionSupplier, final Supplier<Point2D> currentScreenPositionSupplier) {
        source.start(currentScreenDimensionSupplier, currentScreenPositionSupplier, this::onGazeSample);
    }

    @Override
    public void destroy() {
        source.stop();
    }
}
