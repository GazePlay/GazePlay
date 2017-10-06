package tobii;

import gaze.TobiiGazeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import utils.games.Utils;

public class Tobii {

    private static final double screenWidth = com.sun.glass.ui.Screen.getScreens().get(0).getWidth();
    private static final double screenHeight = com.sun.glass.ui.Screen.getScreens().get(0).getWidth();
    private static boolean init = false;

    private static Point2D parseTobiiOutput(String tobiiOutput){

   //     System.out.println(tobiiOutput);
        float x = 0;
        float y = 0;
        try {
            x = Float.valueOf(tobiiOutput.substring(12, 21))*(float)screenWidth;
            y = Float.valueOf(tobiiOutput.substring(22, 29))*(float)screenHeight;
        } catch (Exception e) {

     //       System.out.println(e);
            return null;
        }

        Point2D point = new Point2D(x, y);

        return point;
    }

    public static void execProg(TobiiGazeListener listener){

        if(System.getProperty("os.name").indexOf("indow")<0) {

            return;
        }

        int initialisation = init();

        System.out.println("initialisation : " + initialisation);

        if(initialisation > 0 ) { // init is done : Tobii available

            System.out.println("Tobii detected : YES");

            init = true;

            final Service<Void> calculateService = new Service<Void>() {

                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {

                        @Override
                        protected Void call() throws Exception {

                            Point2D point = null;
                            while(true){
                                try {
                                    Thread.sleep(100);//sleep is mandatory to avoid too much calls to gazePosition()
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                point = parseTobiiOutput(gazePosition());
                                System.out.println(Utils.now());
                                if(point != null)
                                    listener.onGazeUpdate(point);
                            }
                        }
                    };
                }
            };
            calculateService.start();
        }
        else{ //init was not done (no tobii available)
            System.out.println("Tobii detected : NO");
            return;
        }
    }

    public static native int init();

    public static native String gazePosition();

    static{
        try {
            if(System.getProperty("os.name").indexOf("indow")>0) {
                System.load(Utils.getDllFolder()+"tobii_stream_engine.dll");
                System.load(Utils.getDllFolder()+"GazePlayTobiiLibrary2.dll");
                //System.loadLibrary("tobii_stream_engine");
               // System.loadLibrary("GazePlayTobiiLibrary2");
            }
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.out.println("******************************************************");
            System.out.println("Please put appropriate DLLs in DLL folder :");
            System.out.println("tobii_stream_engine.dll and GazePlayTobiiLibrary2.dll");
            System.out.println("should be in");
            System.out.println(Utils.getDllFolder());
            System.out.println("******************************************************");
            System.exit(0);
        }
    }

    public static boolean isInit() {

        return init;
    }

    public static void main(String[] argv){

        System.out.println(parseTobiiOutput("Scene point: 10809754557 0.433731, 0.719170"));
     /*   System.out.println(parseTobiiOutput("Scene point: 10809814527 0.372081, 0.670327"));
        System.out.println(parseTobiiOutput("Scene point: 10809828278 0.373068, 0.672786"));
        System.out.println(parseTobiiOutput("Scene point: 10809874253 0.369580, 0.700685"));
        System.out.println(parseTobiiOutput("Scene point: 10809889523 0.371530, 0.707418"));
        System.out.println(parseTobiiOutput("Scene point: 10817493839 INVALID"));*/

        init();

        while(true){

            System.out.println(gazePosition());
        }


    }


}
