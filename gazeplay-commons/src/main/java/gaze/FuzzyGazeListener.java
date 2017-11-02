package gaze;

import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by schwab on 16/08/2016.
 *
 * may be deprecated
 */
class FuzzyGazeListener implements IGazeListener {

    private static ArrayList<GazeInfos> shapesEventFilter;
    private static ArrayList<GazeInfos> shapesEventHandler;
    private static SecondScreen secondScreen;
    private static long outTime = 0;
    private static final int maxOutLength = 300;

    public FuzzyGazeListener(SecondScreen secondScreen) {

        this.secondScreen = secondScreen;
    }

    public FuzzyGazeListener(ArrayList<GazeInfos> shapesEventFilter, ArrayList<GazeInfos> shapesEventHandler) {

        this.shapesEventFilter = shapesEventFilter;
        this.shapesEventHandler = shapesEventHandler;
    }

    @Override
    public void onGazeUpdate(GazeData gazeData) {
        // Point2D point = GazeUtils.getEyesCenterNormalized(gazeData);

        // log.info(point);
        // log.info("gazedata = " + gazeData.rawCoordinates);

        if (secondScreen != null) {

            secondScreen.light(gazeData.rawCoordinates);
        }
        for (GazeInfos gi : shapesEventFilter) {

            // log.info(gi.getShape().contains(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y));
            // log.info(gi.getShape().sceneToLocal(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y));

            javafx.geometry.Point2D p = gi.getNode().sceneToLocal(gazeData.rawCoordinates.x, gazeData.rawCoordinates.y);

            // if(gi.getShape().contains(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y)){
            if (gi.getNode().contains(p)) {

                outTime = 0;

                if (gi.isOn()) {

                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime()));
                    // log.info(GazeEvent.GAZE_MOVED + " : " + gi.getNode());
                } else {

                    gi.setOn(true);
                    gi.setTime((new Date()).getTime());
                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED));
                    // log.info(GazeEvent.GAZE_ENTERED + " : " + gi.getNode());
                }
            } else {// gaze is not on the shape

                long now = (new Date()).getTime();

                if (gi.isOn()) {// gaze was on the shape previously

                    if (outTime == 0) {

                        outTime = now;
                    } else if (now - outTime < maxOutLength) {// out but not enough

                    } else { // out for a sufficient length
                        gi.setOn(false);
                        gi.setTime(-1);
                        gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED));
                        // log.info(GazeEvent.GAZE_EXITED + " : " + gi.getNode());
                    }
                } else {// gaze was not on the shape previously
                        // nothing to do

                }

            }
        }

    }
}
