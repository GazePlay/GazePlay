package eyetrackersservers;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EyeTribeServer {

    public static void main(final String[] argv) {

        final GazeManager gm = GazeManager.getInstance();
        log.info("" + gm.activate());
        final IGazeListener gazeListener = new GazeListener();
        gm.addGazeListener(gazeListener);
    }

}
