package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Slf4j
public class TobiiGazeDeviceManager extends AbstractGazeDeviceManager {

    private ExecutorService executorService;

    private PositionPollerRunnable positionPollerRunnable;

    public TobiiGazeDeviceManager() {
        super();
    }

    @Override
    public void init(Supplier<Dimension2D> currentScreenDimensionSupplier) {
        positionPollerRunnable = new PositionPollerRunnable(currentScreenDimensionSupplier, this);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(positionPollerRunnable);
    }


    @Override
    public void destroy() {
        positionPollerRunnable.setStopRequested(true);
        ExecutorService executorService = this.executorService;
        if (executorService != null) {
            executorService.shutdown();
        }
    }

}
