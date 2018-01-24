package net.gazeplay.commons.gaze;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 16/08/2016.
 */
class EyeTribeGazeListener extends GazeListener implements IGazeListener {

    private GazeManager gazeManager;

    public EyeTribeGazeListener() {
        super();
    }

    public EyeTribeGazeListener(SecondScreen secondScreen) {
        super(secondScreen);
    }

    @Override
    public void init() {
        gazeManager = GazeManager.getInstance();
        gazeManager.activate();
        gazeManager.addGazeListener(this);
    }

    @Override
    public void destroy() {
        gazeManager.removeGazeListener(this);
    }

    @Override
    public void onGazeUpdate(GazeData gazeData) {
        Point2D point = new Point2D(gazeData.rawCoordinates.x, gazeData.rawCoordinates.y);
        super.onGazeUpdate(point);
    }
}
