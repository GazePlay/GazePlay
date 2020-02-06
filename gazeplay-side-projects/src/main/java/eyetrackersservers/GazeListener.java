package eyetrackersservers;

import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by schwab on 05/08/2017.
 */
@Slf4j
class GazeListener implements IGazeListener {

    public GazeListener() {
    }

    @Override
    public void onGazeUpdate(final GazeData gazeData) {

        log.debug("gazedata = " + gazeData.rawCoordinates);
    }
}
