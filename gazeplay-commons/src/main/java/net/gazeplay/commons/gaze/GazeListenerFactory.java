package net.gazeplay.commons.gaze;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;

/**
 * Created by schwab on 16/08/2016.
 */
@Slf4j
public class GazeListenerFactory {

    @Getter
    private static final GazeListenerFactory instance = new GazeListenerFactory();

    private GazeListenerFactory() {
    }

    public GazeListener createNewGazeListener() {
        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        final String eyetrackerConfigValue = config.getEyetracker();
        final EyeTracker eyeTracker = EyeTracker.valueOf(eyetrackerConfigValue);
        log.info("Eye-tracker = " + eyeTracker);

        final GazeListener gazeListener;

        switch (eyeTracker) {
        case tobii_eyeX_4C:
            gazeListener = new TobiiGazeListener();
            break;
        case eyetribe:
            gazeListener = new EyeTribeGazeListener();
            break;
        default:
            gazeListener = new GazeListener() {
                @Override
                public void init() {

                }

                @Override
                public void destroy() {

                }
            };
        }

        gazeListener.init();
        return gazeListener;
    }

}
