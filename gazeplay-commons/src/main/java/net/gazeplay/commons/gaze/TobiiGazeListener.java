package net.gazeplay.commons.gaze;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 13/09/2017.
 */
public class TobiiGazeListener extends GazeListener {
    public TobiiGazeListener() {
    }

    public TobiiGazeListener(SecondScreen secondScreen) {
        super(secondScreen);
    }

    public TobiiGazeListener(List<GazeInfos> shapesEventFilter, List<GazeInfos> shapesEventHandler) {
        super(shapesEventFilter, shapesEventHandler);
    }

    public TobiiGazeListener(SecondScreen secondScreen, List<GazeInfos> shapesEventFilter,
            ArrayList<GazeInfos> shapesEventHandler) {
        super(secondScreen, shapesEventFilter, shapesEventHandler);
    }
}
