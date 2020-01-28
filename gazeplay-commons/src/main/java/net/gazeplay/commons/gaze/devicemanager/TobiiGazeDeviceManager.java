package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.CachingSupplier;
import net.gazeplay.commons.utils.ScreenDimensionSupplier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Slf4j
public class TobiiGazeDeviceManager extends AbstractGazeDeviceManager {

    private ExecutorService executorService;

    private PositionPollerRunnable positionPollerRunnable;

    public TobiiGazeDeviceManager(Scene scene) {
        super(scene);
    }

    public void init() {
        final Supplier<Dimension2D> screenDimensionSupplier = new CachingSupplier<>(new ScreenDimensionSupplier());
        positionPollerRunnable = new PositionPollerRunnable(screenDimensionSupplier, this);
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
