package net.gazeplay.commons.gaze;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;
import tobii.Tobii;
import tobii.TobiiDemo;

@Slf4j
public class GazeTobii {

    private static boolean init = false;

    public static void execProg(TobiiGazeListener listener) {

        if (!System.getProperty("os.name").contains("indow")) {

            return;
        }

        if (init) {

            return;
        }

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
                                Point2D point = new Point2D(pointAsFloatArray[0], pointAsFloatArray[1]);
                                listener.onGazeUpdate(point);
                            } catch (Exception e) {

                                e.printStackTrace();
                            }
                        }
                    }
                };
            }
        };
        calculateService.start();

        init = true;
    }

    public static boolean isInit() {

        return init;
    }

    public static void main(String[] args) throws Exception {
        TobiiDemo.main(args);
    }

}
