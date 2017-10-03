package gaze;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by schwab on 13/09/2017.
 */
public class TobiiGazeListener {

    private static ArrayList<GazeInfos> shapesEventFilter;
    private static ArrayList<GazeInfos> shapesEventHandler;
    private static SecondScreen secondScreen;

    public TobiiGazeListener(SecondScreen secondScreen){

        this.secondScreen = secondScreen;
    }

    public TobiiGazeListener(ArrayList<GazeInfos> shapesEventFilter, ArrayList<GazeInfos> shapesEventHandler) {

        this.shapesEventFilter = shapesEventFilter;
        this.shapesEventHandler = shapesEventHandler;
    }

    public void onGazeUpdate(Point2D gazePosition){

        //System.out.println(point);
      //  System.out.println("gazedata = " + gazePosition);

        if(secondScreen != null){

            secondScreen.light(gazePosition);
        }
        for(GazeInfos gi : shapesEventFilter){

            javafx.geometry.Point2D p = gi.getNode().sceneToLocal(gazePosition.getX(),gazePosition.getY());

        //    System.out.println("p = " + p);

            if(gi.getNode().contains(p)){

                if(gi.isOn()){

                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(), gazePosition.getX(),gazePosition.getY()));
                    System.out.println(GazeEvent.GAZE_MOVED + " : " + gi.getNode());
                }
                else {

                    gi.setOn(true);
                    gi.setTime((new Date()).getTime());
                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, gi.getTime(), gazePosition.getX(),gazePosition.getY()));
                    System.out.println(GazeEvent.GAZE_ENTERED + " : " + gi.getNode());
                }
            }
            else{//gaze is not on the shape

                if(gi.isOn()){//gaze was on the shape previously

                    gi.setOn(false);
                    gi.setTime(-1);
                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), gazePosition.getX(),gazePosition.getY()));
                    System.out.println(GazeEvent.GAZE_EXITED + " : " + gi.getNode());
                }
                else{//gaze was not on the shape previously
                    //nothing to do

                }

            }
        }

    }
}