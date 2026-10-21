package net.gazeplay.commons.gaze.devicemanager;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * EyeTribe eye trackers, through the EyeTribe SDK, which calls {@link #onGazeUpdate(GazeData)} on its own thread.
 */
class EyeTribeGazeSource implements GazeSource, IGazeListener {

    private GazeManager gazeManager;

    private Consumer<GazeSample> consumer = sample -> {
    };

    @Override
    public void start(final Supplier<Dimension2D> screenDimension, final Supplier<Point2D> screenPosition, final Consumer<GazeSample> consumer) {
        this.consumer = consumer;
        gazeManager = GazeManager.getInstance();
        gazeManager.activate();
        gazeManager.addGazeListener(this);
    }

    @Override
    public void stop() {
        if (gazeManager != null) {
            gazeManager.removeGazeListener(this);
        }
    }

    @Override
    public void onGazeUpdate(final GazeData gazeData) {
        consumer.accept(GazeSample.now(new Point2D(gazeData.rawCoordinates.x, gazeData.rawCoordinates.y), GazeSample.Source.GAZE));
    }
}
