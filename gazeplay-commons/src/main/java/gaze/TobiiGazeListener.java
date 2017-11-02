package gaze;

import java.util.ArrayList;

/**
 * Created by schwab on 13/09/2017.
 */
public class TobiiGazeListener extends GazeListener {
    public TobiiGazeListener() {
    }

    public TobiiGazeListener(SecondScreen secondScreen) {
        super(secondScreen);
    }

    public TobiiGazeListener(ArrayList<GazeInfos> shapesEventFilter, ArrayList<GazeInfos> shapesEventHandler) {
        super(shapesEventFilter, shapesEventHandler);
    }

    public TobiiGazeListener(SecondScreen secondScreen, ArrayList<GazeInfos> shapesEventFilter,
            ArrayList<GazeInfos> shapesEventHandler) {
        super(secondScreen, shapesEventFilter, shapesEventHandler);
    }
}