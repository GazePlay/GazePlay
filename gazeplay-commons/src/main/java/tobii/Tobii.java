package tobii;

import gaze.TobiiGazeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;
import utils.games.Utils;

@Slf4j
public class Tobii {

    private static final double screenWidth = com.sun.glass.ui.Screen.getScreens().get(0).getWidth();
    private static final double screenHeight = com.sun.glass.ui.Screen.getScreens().get(0).getWidth();
    private static boolean init = false;

    private static Point2D parseTobiiOutput(String tobiiOutput) {

        // log.info(tobiiOutput);
        float x = 0;
        float y = 0;
        try {
            x = Float.valueOf(tobiiOutput.substring(12, 21)) * (float) screenWidth;
            y = Float.valueOf(tobiiOutput.substring(22, 29)) * (float) screenHeight;
        } catch (Exception e) {

            // log.info(e);
            return null;
        }

        Point2D point = new Point2D(x, y);

        return point;
    }

    public static void execProg(TobiiGazeListener listener) {

        if (System.getProperty("os.name").indexOf("indow") < 0) {

            return;
        }

        int initialisation = init();

        log.info("initialisation : " + initialisation);

        if (initialisation > 0) { // init is done : Tobii available

            log.info("Tobii detected : YES");

            init = true;

            final Service<Void> calculateService = new Service<Void>() {

                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {

                        @Override
                        protected Void call() throws Exception {

                            Point2D point = null;
                            while (true) {

                                try {
                                    Thread.sleep(10);// sleep is mandatory to avoid too much calls to gazePosition()
                                    point = parseTobiiOutput(gazePosition());
                                    // log.info("Tobii : " + Utils.now() + " " + point);
                                    if (point != null)
                                        listener.onGazeUpdate(point);
                                } catch (Exception e) {
                                    // log.error("Exception", e);
                                }
                            }
                        }
                    };
                }
            };
            calculateService.start();
        } else { // init was not done (no tobii available)
            log.info("Tobii detected : NO");
            return;
        }
    }

    public static native int init();

    public static native String gazePosition();

    static {
        try {
            if (System.getProperty("os.name").indexOf("indow") > 0) {
                System.load(Utils.getDllFolder() + "tobii_stream_engine.dll");
                System.load(Utils.getDllFolder() + "GazePlayTobiiLibrary2.dll");
                // System.loadLibrary("tobii_stream_engine");
                // System.loadLibrary("GazePlayTobiiLibrary2");
            }
        } catch (java.lang.UnsatisfiedLinkError e) {
            log.info("******************************************************");
            log.info("If you wish to Use a Tobii 4C or a Tobii EyeX");
            log.info("Please put appropriate DLLs in DLL folder :");
            log.info("tobii_stream_engine.dll and GazePlayTobiiLibrary2.dll");
            log.info("should be in");
            log.info(Utils.getDllFolder());
            log.info("******************************************************");
            // System.exit(0);
        }
    }

    public static boolean isInit() {

        return init;
    }

    public static void main(String[] argv) {

        log.info("" + parseTobiiOutput("Scene point: 10809754557 0.433731, 0.719170"));
        /*
         * log.info(parseTobiiOutput("Scene point: 10809814527 0.372081, 0.670327"));
         * log.info(parseTobiiOutput("Scene point: 10809828278 0.373068, 0.672786"));
         * log.info(parseTobiiOutput("Scene point: 10809874253 0.369580, 0.700685"));
         * log.info(parseTobiiOutput("Scene point: 10809889523 0.371530, 0.707418"));
         * log.info(parseTobiiOutput("Scene point: 10817493839 INVALID"));
         */

        init();

        while (true) {

            log.info(gazePosition());
        }

    }

}
