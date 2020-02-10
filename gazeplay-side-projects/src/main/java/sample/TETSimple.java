package sample;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.utils.GazeUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by schwab on 13/08/2016.
 */
public class TETSimple {

    public static void main(final String[] args) {

        final GazeManager gm = GazeManager.getInstance();
        gm.activate();
        final GazeListener gazeListener = new GazeListener();
        gm.addGazeListener(gazeListener);
    }
}

@Slf4j
class GazeListener implements IGazeListener {

    @Override
    public void onGazeUpdate(final GazeData gazeData) {
        log.info("" + GazeUtils.getEyesCenterNormalized(gazeData));
    }
}
