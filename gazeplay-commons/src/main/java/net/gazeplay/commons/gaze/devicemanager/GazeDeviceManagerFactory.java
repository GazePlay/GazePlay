package net.gazeplay.commons.gaze.devicemanager;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class GazeDeviceManagerFactory {

    private final AtomicReference<GazeDeviceManager> currentInstanceReference = new AtomicReference<>();

    public GazeDeviceManagerFactory() {
    }

    public synchronized GazeDeviceManager get(Scene scene) {
        GazeDeviceManager gazeDeviceManager = currentInstanceReference.get();
        if (gazeDeviceManager != null) {
            gazeDeviceManager.clear();
            gazeDeviceManager.destroy();
        }
        gazeDeviceManager = create(scene);
        currentInstanceReference.set(gazeDeviceManager);
        return gazeDeviceManager;
    }

    private GazeDeviceManager create(Scene scene) {
        final Configuration config = ActiveConfigurationContext.getInstance();

        final String eyetrackerConfigValue = config.getEyeTracker();
        final EyeTracker eyeTracker = EyeTracker.valueOf(eyetrackerConfigValue);
        log.info("Eye-tracker = " + eyeTracker);

        final GazeDeviceManager gazeDeviceManager;

        switch (eyeTracker) {
            case tobii_eyeX_4C:
                gazeDeviceManager = new TobiiGazeDeviceManager(scene);
                break;
            case eyetribe:
                gazeDeviceManager = new EyeTribeGazeDeviceManager(scene);
                break;
            default:
                gazeDeviceManager = new AbstractGazeDeviceManager(scene) {
                    @Override
                    public void init() {
                    }

                    @Override
                    public void destroy() {

                    }

                };
        }

        gazeDeviceManager.init();
        return gazeDeviceManager;
    }

}
