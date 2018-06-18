package net.gazeplay.commons.gaze.devicemanager;

import com.sun.glass.ui.Screen;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;
import tobii.Tobii;

@Slf4j
public class TobiiGazeDeviceManager extends AbstractGazeDeviceManager {

    private Service<Void> calculateService;

    private transient boolean stopRequested = false;

    public TobiiGazeDeviceManager() {
        super();
    }

    public void init() {
        Tobii.gazePosition();

        Screen mainScreen = Screen.getMainScreen();
        final int screenWidth = mainScreen.getWidth();
        final int screenHeight = mainScreen.getHeight();

        calculateService = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() {
                        while (!stopRequested) {
                            float[] pointAsFloatArray = Tobii.gazePosition();

                            final float xRatio = pointAsFloatArray[0];
                            final float yRatio = pointAsFloatArray[1];

                            final double positionX = xRatio * screenWidth;
                            final double positionY = yRatio * screenHeight;

                            Point2D point = new Point2D(positionX, positionY);
                            onGazeUpdate(point);

                            // sleep is mandatory to avoid too much calls to gazePosition()
                            try {
                                Thread.sleep(25);
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
                ;
            // calculateService.reset();
        }
    }

}
