package gaze;

import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by schwab on 16/08/2016.
 */
class TrueGazeListener implements IGazeListener {

    private static ArrayList<GazeInfos> shapesEventFilter;
    private static ArrayList<GazeInfos> shapesEventHandler;
    private static SecondScreen secondScreen;

    public TrueGazeListener(SecondScreen secondScreen){

        this.secondScreen = secondScreen;
    }

    public TrueGazeListener(ArrayList<GazeInfos> shapesEventFilter, ArrayList<GazeInfos> shapesEventHandler) {

        this.shapesEventFilter = shapesEventFilter;
        this.shapesEventHandler = shapesEventHandler;
    }

    @Override
    public void onGazeUpdate(GazeData gazeData){
        //Point2D point = GazeUtils.getEyesCenterNormalized(gazeData);

        //System.out.println(point);
        System.out.println("gazedata = " + gazeData.rawCoordinates);
        if(secondScreen != null){

            secondScreen.light(gazeData.rawCoordinates);
        }
        for(GazeInfos gi : shapesEventFilter){

           // System.out.println(gi.getShape().contains(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y));
           // System.out.println(gi.getShape().sceneToLocal(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y));

            javafx.geometry.Point2D p = gi.getNode().sceneToLocal(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y);

            //if(gi.getShape().contains(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y)){
            if(gi.getNode().contains(p)){

                if(gi.isOn()){

                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(), gazeData.rawCoordinates.x,gazeData.rawCoordinates.y));
                    //System.out.println(GazeEvent.GAZE_MOVED + " : " + gi.getNode());
                }
                else {

                    gi.setOn(true);
                    gi.setTime((new Date()).getTime());
                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, gi.getTime(), gazeData.rawCoordinates.x,gazeData.rawCoordinates.y));
                    //System.out.println(GazeEvent.GAZE_ENTERED + " : " + gi.getNode());
                }
            }
            else{//gaze is not on the shape

                if(gi.isOn()){//gaze was on the shape previously

                    gi.setOn(false);
                    gi.setTime(-1);
                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), gazeData.rawCoordinates.x,gazeData.rawCoordinates.y));
                    //System.out.println(GazeEvent.GAZE_EXITED + " : " + gi.getNode());
                }
                else{//gaze was not on the shape previously
                    //nothing to do

                }

            }
        }

    }
}