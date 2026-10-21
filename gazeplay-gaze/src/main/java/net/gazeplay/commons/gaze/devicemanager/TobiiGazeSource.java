package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Tobii eye trackers, through the TobiiStreamEngineForJava wrapper: polled every 10 ms on a dedicated thread (see
 * {@link PositionPollerRunnable}).
 */
@Slf4j
public class TobiiGazeSource implements GazeSource {

    private ExecutorService executorService;

    private PositionPollerRunnable positionPollerRunnable;

    @Override
    public void start(final Supplier<Dimension2D> screenDimension, final Supplier<Point2D> screenPosition, final Consumer<GazeSample> consumer) {
        positionPollerRunnable = new PositionPollerRunnable(screenDimension, screenPosition, consumer);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(positionPollerRunnable);
    }

    @Override
    public void stop() {
        if (positionPollerRunnable != null) {
            positionPollerRunnable.setStopRequested(true);
        }
        ExecutorService executorService = this.executorService;
        if (executorService != null) {
            executorService.shutdown();
        }
    }

}
