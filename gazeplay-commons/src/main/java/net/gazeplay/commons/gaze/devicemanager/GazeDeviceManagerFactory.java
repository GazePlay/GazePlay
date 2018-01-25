package net.gazeplay.commons.gaze.devicemanager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Created by schwab on 16/08/2016.
 */
@Slf4j
public class GazeDeviceManagerFactory {

    @Getter
    private static final GazeDeviceManagerFactory instance = new GazeDeviceManagerFactory();

    private GazeDeviceManagerFactory() {
    }

    public GazeDeviceManager createNewGazeListener() {
        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        final String eyetrackerConfigValue = config.getEyetracker();
        final EyeTracker eyeTracker = EyeTracker.valueOf(eyetrackerConfigValue);
        log.info("Eye-tracker = " + eyeTracker);

        final GazeDeviceManager gazeDeviceManager;

        switch (eyeTracker) {
        case tobii_eyeX_4C:
            gazeDeviceManager = new TobiiGazeDeviceManager();
            break;
        case eyetribe:
            gazeDeviceManager = new EyeTribeGazeDeviceManager();
            break;
        default:
            gazeDeviceManager = new AbstractGazeDeviceManager() {
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
