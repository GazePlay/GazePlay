package net.gazeplay.commons.gaze;

import com.theeyetribe.clientsdk.GazeManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;

/**
 * Created by schwab on 16/08/2016.
 */
@Slf4j
public class GazeUtils {

    @Getter
    private static final GazeUtils instance = new GazeUtils();

    private GazeUtils() {
    }

    public GazeListener createNewGazeListener() {
        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        final String eyetrackerConfigValue = config.getEyetracker();
        final EyeTracker eyeTracker = EyeTracker.valueOf(eyetrackerConfigValue);
        log.info("Eye-tracker = " + eyeTracker);

        final GazeListener gazeListener;

        switch (eyeTracker) {
        case tobii_eyeX_4C:
            GazeTobii gazeTobii = new GazeTobii();
            TobiiGazeListener tobiiGazeListener = new TobiiGazeListener();
            gazeTobii.execProg(tobiiGazeListener);
            gazeListener = tobiiGazeListener;
            break;
        case eyetribe:
            EyeTribeGazeListener eyeTribeGazeListener = new EyeTribeGazeListener();
            GazeManager gazeManager = GazeManager.getInstance();
            gazeManager.activate();
            gazeManager.addGazeListener(eyeTribeGazeListener);
            gazeListener = eyeTribeGazeListener;
            break;
        default:
            gazeListener = new GazeListener() {
            };
        }

        return gazeListener;
    }

}
