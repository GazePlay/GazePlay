package tobii;

import javafx.geometry.Point2D;

public class Tobii {

    private static Point2D parseTobiiOutput(String tobiiOutput){

        System.out.println(tobiiOutput);

        float x = 0;
        float y = 0;
        try {
            x = Float.valueOf(tobiiOutput.substring(24, 32));
            y = Float.valueOf(tobiiOutput.substring(34, 42));
        } catch (Exception e) {

            return null;
        }

        System.out.println(x);
        System.out.println(y);

        Point2D point = new Point2D(x,y);

        System.out.println(point);

        return point;
    }

    public static void execProg() throws Exception{

        Runtime runtime = Runtime.getRuntime();
        runtime.exec(new String[] { "C:\\Program Files\\MonAppli\\monappli.exe" } );



    }

    public static void main(String[] argv){

        parseTobiiOutput("Gaze point: 10809754557 0.433731, 0.719170");
        parseTobiiOutput("Gaze point: 10809814527 0.372081, 0.670327");
        parseTobiiOutput("Gaze point: 10809828278 0.373068, 0.672786");
        parseTobiiOutput("Gaze point: 10809874253 0.369580, 0.700685");
        parseTobiiOutput("Gaze point: 10809889523 0.371530, 0.707418");
        parseTobiiOutput("Gaze point: 10817493839 INVALID");

    }


}
