package net.gazeplay.commons.gaze;

import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 16/08/2016.
 */
class EyeTribeGazeListener extends GazeListener implements IGazeListener {

    public EyeTribeGazeListener() {
    }

    public EyeTribeGazeListener(SecondScreen secondScreen) {
        super(secondScreen);
    }

    public EyeTribeGazeListener(List<GazeInfos> shapesEventFilter, List<GazeInfos> shapesEventHandler) {
        super(shapesEventFilter, shapesEventHandler);
    }

    public EyeTribeGazeListener(SecondScreen secondScreen, ArrayList<GazeInfos> shapesEventFilter,
            ArrayList<GazeInfos> shapesEventHandler) {
        super(secondScreen, shapesEventFilter, shapesEventHandler);
    }

    @Override
    public void onGazeUpdate(GazeData gazeData) {

        Point2D point = new Point2D(gazeData.rawCoordinates.x, gazeData.rawCoordinates.y);
        super.onGazeUpdate(point);
    }
}
