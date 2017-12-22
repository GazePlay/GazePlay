package net.gazeplay.commons.gaze;

import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by schwab on 04/10/2017.
 */
@Slf4j
public abstract class GazeListener {

    protected static ArrayList<GazeInfos> shapesEventFilter;
    protected static ArrayList<GazeInfos> shapesEventHandler;
    protected static SecondScreen secondScreen;

    public GazeListener() {
    }

    public GazeListener(SecondScreen secondScreen) {

        this.secondScreen = secondScreen;
    }

    public GazeListener(ArrayList<GazeInfos> shapesEventFilter, ArrayList<GazeInfos> shapesEventHandler) {

        this.shapesEventFilter = shapesEventFilter;
        this.shapesEventHandler = shapesEventHandler;
    }

    public GazeListener(SecondScreen secondScreen, ArrayList<GazeInfos> shapesEventFilter,
            ArrayList<GazeInfos> shapesEventHandler) {

        this.secondScreen = secondScreen;
        this.shapesEventFilter = shapesEventFilter;
        this.shapesEventHandler = shapesEventHandler;
    }

    public void onGazeUpdate(Point2D gazePosition) {

        // log.info("gazedata = " + gazePosition);

        if (secondScreen != null) {

            secondScreen.light(gazePosition);
        }

        if (GazeUtils.stats != null) {

            GazeUtils.stats.incHeatMap((int) gazePosition.getX(), (int) gazePosition.getY());
        }

        for (GazeInfos gi : shapesEventFilter) {

            javafx.geometry.Point2D p = gi.getNode().sceneToLocal(gazePosition.getX(), gazePosition.getY());

            // log.info("p = " + p);

            if (gi.getNode().contains(p)) {

                if (gi.isOn()) {

                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(), gazePosition.getX(),
                            gazePosition.getY()));
                    // log.info(GazeEvent.GAZE_MOVED + " : " + gi.getNode());
                } else {

                    gi.setOn(true);
                    gi.setTime((new Date()).getTime());
                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, gi.getTime(), gazePosition.getX(),
                            gazePosition.getY()));
                    // log.info(GazeEvent.GAZE_ENTERED + " : " + gi.getNode());
                }
            } else {// gaze is not on the shape

                if (gi.isOn()) {// gaze was on the shape previously

                    gi.setOn(false);
                    gi.setTime(-1);
                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), gazePosition.getX(),
                            gazePosition.getY()));
                    // log.info(GazeEvent.GAZE_EXITED + " : " + gi.getNode());
                } else {// gaze was not on the shape previously
                        // nothing to do

                }

            }
        }

    }
}
