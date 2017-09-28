package tobii;

import gaze.TobiiGazeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;

public class Tobii {

    private static final double screenWidth = com.sun.glass.ui.Screen.getScreens().get(0).getWidth();
    private static final double screenHeight = com.sun.glass.ui.Screen.getScreens().get(0).getWidth();

    private static Point2D parseTobiiOutput(String tobiiOutput) {

        System.out.println(tobiiOutput);
        float x = 0;
        float y = 0;
        try {
            x = Float.valueOf(tobiiOutput.substring(12, 21))*(float)screenWidth;
            y = Float.valueOf(tobiiOutput.substring(22, 29))*(float)screenHeight;
        } catch (Exception e) {

            System.out.println(e);
            return null;
        }

        Point2D point = new Point2D(x, y);

        return point;
    }

    public static void execProg(TobiiGazeListener listener) {
        init();

        final Service<Void> calculateService = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        while(true){

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println(parseTobiiOutput(gazePosition()));
                        }
                    }
                };
            }
        };
        calculateService.start();
    }

    public static native void init();

    public static native String gazePosition();


    static{

        try {
            System.loadLibrary("tobii_stream_engine");
            System.loadLibrary("GazePlayTobiiLibrary2");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] argv){

        System.out.println(parseTobiiOutput("Gaze point: 10809754557 0.433731, 0.719170"));
     /*   System.out.println(parseTobiiOutput("Gaze point: 10809814527 0.372081, 0.670327"));
        System.out.println(parseTobiiOutput("Gaze point: 10809828278 0.373068, 0.672786"));
        System.out.println(parseTobiiOutput("Gaze point: 10809874253 0.369580, 0.700685"));
        System.out.println(parseTobiiOutput("Gaze point: 10809889523 0.371530, 0.707418"));
        System.out.println(parseTobiiOutput("Gaze point: 10817493839 INVALID"));*/

        init();

        while(true){

            System.out.println(gazePosition());
        }


    }


}
