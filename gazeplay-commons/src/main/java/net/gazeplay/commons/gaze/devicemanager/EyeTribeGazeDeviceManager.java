package net.gazeplay.commons.gaze.devicemanager;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;

import java.util.function.Supplier;

/**
 * Created by schwab on 16/08/2016.
 */
class EyeTribeGazeDeviceManager extends AbstractGazeDeviceManager implements IGazeListener {

    private GazeManager gazeManager;

    public EyeTribeGazeDeviceManager() {
        super();
    }

    @Override
    public void init(Supplier<Dimension2D> currentScreenDimensionSupplier) {
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
