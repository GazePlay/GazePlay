package gaze;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by schwab on 04/10/2017.
 */
public abstract class GazeListener {

    protected static ArrayList<GazeInfos> shapesEventFilter;
    protected static ArrayList<GazeInfos> shapesEventHandler;
    protected static SecondScreen secondScreen;

    public GazeListener() {
    }

    public GazeListener(SecondScreen secondScreen){

        this.secondScreen = secondScreen;
    }

    public GazeListener(ArrayList<GazeInfos> shapesEventFilter, ArrayList<GazeInfos> shapesEventHandler) {

        this.shapesEventFilter = shapesEventFilter;
        this.shapesEventHandler = shapesEventHandler;
    }

    public GazeListener(SecondScreen secondScreen, ArrayList<GazeInfos> shapesEventFilter, ArrayList<GazeInfos> shapesEventHandler) {

        this.secondScreen = secondScreen;
        this.shapesEventFilter = shapesEventFilter;
        this.shapesEventHandler = shapesEventHandler;
    }

    public void onGazeUpdate(Point2D gazePosition){

    }
}
