package net.gazeplay.commons.gaze.devicemanager;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.CachingSupplier;
import net.gazeplay.commons.utils.ScreenDimensionSupplier;
import tobii.Tobii;

import java.util.function.Supplier;

@Slf4j
public class TobiiGazeDeviceManager extends AbstractGazeDeviceManager {

    private Service<Void> calculateService;

    private transient boolean stopRequested = false;

    public TobiiGazeDeviceManager() {
        super();
    }

    public void init() {
        Tobii.gazePosition();

        final Supplier<Dimension2D> screenDimensionSupplier = new CachingSupplier<>(new ScreenDimensionSupplier());

        calculateService = new Service<>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<>() {

                    @Override
                    protected Void call() {

                        final Dimension2D screenDimension = screenDimensionSupplier.get();
                        while (!stopRequested) {
                            float[] pointAsFloatArray = Tobii.gazePosition();

                            final float xRatio = pointAsFloatArray[0];
                            final float yRatio = pointAsFloatArray[1];

                            final double positionX = xRatio * screenDimension.getWidth();
                            final double positionY = yRatio * screenDimension.getHeight();

                            Point2D point = new Point2D(positionX, positionY);
                            Platform.runLater(() -> onGazeUpdate(point));

                            // sleep is mandatory to avoid too much calls to gazePosition()
                            try {
                                Thread.sleep(10);
                                Configuration config = ActiveConfigurationContext.getInstance();
                                if (config.isGazeMenuEnable()) {
                                    Thread.sleep(10);
                                }
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                        return null;
                    }
                };
            }
        };

        calculateService.start();
    }

    @Override
    public void destroy() {
        stopRequested = true;
        Service<Void> calculateService = this.calculateService;
        if (calculateService != null) {
            while (!calculateService.cancel())
                calculateService.reset();
        }
    }

}
