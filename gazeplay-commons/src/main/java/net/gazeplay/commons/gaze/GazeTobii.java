package net.gazeplay.commons.gaze;

import com.sun.glass.ui.Screen;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tobii.Tobii;
import tobii.TobiiDemo;

@Slf4j
public class GazeTobii {

    public void execProg(TobiiGazeListener listener) {

        Tobii.gazePosition();

        Screen mainScreen = Screen.getMainScreen();
        final int screenWidth = mainScreen.getWidth();
        final int screenHeight = mainScreen.getHeight();

        final Service<Void> calculateService = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        while (true) {
                            try {
                                Thread.sleep(10);// sleep is mandatory to avoid too much calls to gazePosition()
                                float[] pointAsFloatArray = Tobii.gazePosition();

                                final float xRatio = pointAsFloatArray[0];
                                final float yRatio = pointAsFloatArray[1];

                                final double positionX = xRatio * screenWidth;
                                final double positionY = yRatio * screenHeight;

                                Point2D point = new Point2D(positionX, positionY);
                                listener.onGazeUpdate(point);
                            } catch (Throwable e) {
                                log.error("Exception on Gaze position update", e);
                            }
                        }
                    }
                };
            }
        };

        calculateService.start();
    }

    public static void main(String[] args) throws Exception {
        TobiiDemo.main(args);
    }

}
