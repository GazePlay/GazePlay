package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Component
@Slf4j
public class GazeDeviceManagerFactory {

    private final AtomicReference<GazeDeviceManager> currentInstanceReference = new AtomicReference<>();

    @Autowired
    @Setter
    private Supplier<Dimension2D> currentScreenDimensionSupplier;

    @Autowired
    @Setter
    private Supplier<Point2D> currentScreenPositionSupplier;

    public GazeDeviceManagerFactory() {
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
                gazeDeviceManager = new TobiiGazeDeviceManager();
                break;
            case eyetribe:
                gazeDeviceManager = new EyeTribeGazeDeviceManager();
                break;
            default:
                gazeDeviceManager = new AbstractGazeDeviceManager() {
                    @Override
                    public void init(final Supplier<Dimension2D> currentScreenDimensionSupplier, final Supplier<Point2D> currentScreenPositionSupplier) {
                    }

                    @Override
                    public void destroy() {

                    }

                };
        }

        gazeDeviceManager.init(currentScreenDimensionSupplier, currentScreenPositionSupplier);
        return gazeDeviceManager;
    }

}
