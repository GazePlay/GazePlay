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
        final EyeTracker eyeTracker = EyeTracker.valueOf(eyetrackerConfigValue);
        log.info("Eye-tracker = " + eyeTracker);

        final GazeDeviceManager gazeDeviceManager;

        switch (eyeTracker) {
            case tobii:
                gazeDeviceManager = new GazeSourceDeviceManager(new TobiiGazeSource());
                break;
            case eyetribe:
                gazeDeviceManager = new GazeSourceDeviceManager(new EyeTribeGazeSource());
                break;
            default:
                // mouse_control: no gaze sample (the games use the mouse events directly)
                gazeDeviceManager = new GazeSourceDeviceManager(GazeSource.NONE);
        }

        gazeDeviceManager.init(currentScreenDimensionSupplier, currentScreenPositionSupplier);
        return gazeDeviceManager;
    }

}
