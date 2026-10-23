package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
public class GazeDeviceManagerFactory {

    private final AtomicReference<GazeDeviceManager> currentInstanceReference = new AtomicReference<>();

    @Setter
    private Supplier<Dimension2D> currentScreenDimensionSupplier;

    @Setter
    private Supplier<Point2D> currentScreenPositionSupplier;

    public GazeDeviceManagerFactory() {
    }

    public GazeDeviceManagerFactory(final Supplier<Dimension2D> currentScreenDimensionSupplier,
                                    final Supplier<Point2D> currentScreenPositionSupplier) {
        this.currentScreenDimensionSupplier = currentScreenDimensionSupplier;
        this.currentScreenPositionSupplier = currentScreenPositionSupplier;
    }

    public synchronized GazeDeviceManager get() {
        GazeDeviceManager gazeDeviceManager = currentInstanceReference.get();
        if (gazeDeviceManager != null) {
            gazeDeviceManager.clear();
            gazeDeviceManager.destroy();
        }
        gazeDeviceManager = create();
        currentInstanceReference.set(gazeDeviceManager);
        return gazeDeviceManager;
    }

    private GazeDeviceManager create() {
        final Configuration config = ActiveConfigurationContext.getInstance();

        final String eyetrackerConfigValue = config.getEyeTracker();
        final EyeTracker eyeTracker = EyeTracker.fromConfigValue(eyetrackerConfigValue);
        log.info("Eye-tracker = " + eyeTracker);

        // mouse_control: no gaze sample (the games use the mouse events directly). Whether the mouse also acts in the
        // games is decided by the game screen (GameContextFactory)
        final GazeDeviceManager gazeDeviceManager = eyeTracker.usesGaze()
            ? new GazeSourceDeviceManager(new TobiiGazeSource())
            : new GazeSourceDeviceManager(GazeSource.NONE);

        gazeDeviceManager.init(currentScreenDimensionSupplier, currentScreenPositionSupplier);
        return gazeDeviceManager;
    }

}
