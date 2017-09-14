package tobii;

import gaze.SecondScreen;
import gaze.TobiiGazeListener;
import javafx.geometry.Point2D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Tobii {

    private static final double screenWidth = com.sun.glass.ui.Screen.getScreens().get(0).getWidth();
    private static final double screenHeight = com.sun.glass.ui.Screen.getScreens().get(0).getWidth();

    private static Point2D parseTobiiOutput(String tobiiOutput) {

      //  System.out.println(tobiiOutput);

        float x = 0;
        float y = 0;
        try {
            x = Float.valueOf(tobiiOutput.substring(24, 32))*(float)screenWidth;
            y = Float.valueOf(tobiiOutput.substring(34, 42))*(float)screenHeight;
        } catch (Exception e) {

            return null;
        }

        Point2D point = new Point2D(x, y);

        return point;
    }

    public static void execProg(TobiiGazeListener listener) {

        Runtime runtime = Runtime.getRuntime();

        Process process = null;
        try {
            process = runtime.exec(new String[]{"C:\\Users\\schwab\\IdeaProjects\\GazePlay\\tobii\\GazePlay-tobii.exe"});
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";

        int i = 0;

        try {
            while ((line = reader.readLine()) != null) {
                // Traitement du flux de sortie de l'application si besoin est

               if(i++==1000)
                    System.out.println(parseTobiiOutput(line));
                Point2D gazePosition = parseTobiiOutput(line);
                if(gazePosition!=null){

                    listener.onGazeUpdate(gazePosition);
                }
            }
        }catch(Exception ioe){
        ioe.printStackTrace();
        }
    }

    public static void main(String[] argv){

     /*   System.out.println(parseTobiiOutput("Gaze point: 10809754557 0.433731, 0.719170"));
        System.out.println(parseTobiiOutput("Gaze point: 10809814527 0.372081, 0.670327"));
        System.out.println(parseTobiiOutput("Gaze point: 10809828278 0.373068, 0.672786"));
        System.out.println(parseTobiiOutput("Gaze point: 10809874253 0.369580, 0.700685"));
        System.out.println(parseTobiiOutput("Gaze point: 10809889523 0.371530, 0.707418"));
        System.out.println(parseTobiiOutput("Gaze point: 10817493839 INVALID"));*/

        //execProg();

    }


}
