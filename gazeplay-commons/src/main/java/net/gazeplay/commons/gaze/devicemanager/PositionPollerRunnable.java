package net.gazeplay.commons.gaze.devicemanager;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import tobii.Tobii;

import java.util.function.Supplier;

@Slf4j
public class PositionPollerRunnable implements Runnable {

    private final Supplier<Dimension2D> screenDimensionSupplier;

    private final TobiiGazeDeviceManager tobiiGazeDeviceManager;

    @Setter
    private transient boolean stopRequested = false;

    public PositionPollerRunnable(final Supplier<Dimension2D> screenDimensionSupplier, final TobiiGazeDeviceManager tobiiGazeDeviceManager) {
        this.screenDimensionSupplier = screenDimensionSupplier;
        this.tobiiGazeDeviceManager = tobiiGazeDeviceManager;
    }

    @Override
    public void run() {
        while (!stopRequested) {
            try {
                poll();
            } catch (final RuntimeException e) {
                log.warn("Exception while polling position of gaze", e);
            }

            // sleep is mandatory to avoid too much calls to gazePosition()
            try {
                Thread.sleep(10);
                final Configuration config = ActiveConfigurationContext.getInstance();
                if (config.isGazeMenuEnable()) {
                    Thread.sleep(10);
                }
            } catch (InterruptedException | RuntimeException e) {
                log.warn("Exception while sleeping until next poll", e);
            }
        }
    }

    private void poll() {
        final float[] pointAsFloatArray = Tobii.gazePosition();

        final float xRatio = pointAsFloatArray[0];
        final float yRatio = pointAsFloatArray[1];

        final Dimension2D screenDimension = screenDimensionSupplier.get();
        final double positionX = xRatio * screenDimension.getWidth();
        final double positionY = yRatio * screenDimension.getHeight();

        final Point2D point = new Point2D(positionX, positionY);
        Platform.runLater(() -> tobiiGazeDeviceManager.onGazeUpdate(point));
    }

}
