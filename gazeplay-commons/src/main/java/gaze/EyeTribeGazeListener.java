package gaze;

import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import javafx.geometry.Point2D;

import java.util.ArrayList;

/**
 * Created by schwab on 16/08/2016.
 */
class EyeTribeGazeListener extends GazeListener implements IGazeListener {

    public EyeTribeGazeListener() {
    }

    public EyeTribeGazeListener(SecondScreen secondScreen) {
        super(secondScreen);
    }

    public EyeTribeGazeListener(ArrayList<GazeInfos> shapesEventFilter, ArrayList<GazeInfos> shapesEventHandler) {
        super(shapesEventFilter, shapesEventHandler);
    }

    public EyeTribeGazeListener(SecondScreen secondScreen, ArrayList<GazeInfos> shapesEventFilter,
            ArrayList<GazeInfos> shapesEventHandler) {
        super(secondScreen, shapesEventFilter, shapesEventHandler);
    }

    @Override
    public void onGazeUpdate(GazeData gazeData) {

        Point2D point = new Point2D(gazeData.rawCoordinates.x, gazeData.rawCoordinates.y);
        super.onGazeUpdate(point);
    }
    /*
     * public void onGazeUpdateold(GazeData gazeData){ //Point2D point = GazeUtils.getEyesCenterNormalized(gazeData);
     * 
     * //log.info(point); //log.info("gazedata = " + gazeData.rawCoordinates); if(secondScreen != null){
     * 
     * secondScreen.light(gazeData.rawCoordinates); }
     * 
     * if(GazeUtils.stats != null) {
     * 
     * GazeUtils.stats.incHeatMap((int)gazeData.rawCoordinates.x, (int)gazeData.rawCoordinates.y); }
     * 
     * for(GazeInfos gi : shapesEventFilter){
     * 
     * // log.info(gi.getShape().contains(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y)); //
     * log.info(gi.getShape().sceneToLocal(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y));
     * 
     * javafx.geometry.Point2D p = gi.getNode().sceneToLocal(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y);
     * 
     * //if(gi.getShape().contains(gazeData.rawCoordinates.x,gazeData.rawCoordinates.y)){ if(gi.getNode().contains(p)){
     * 
     * if(gi.isOn()){
     * 
     * gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(),
     * gazeData.rawCoordinates.x,gazeData.rawCoordinates.y)); //log.info(GazeEvent.GAZE_MOVED + " : " + gi.getNode()); }
     * else {
     * 
     * gi.setOn(true); gi.setTime((new Date()).getTime()); gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED,
     * gi.getTime(), gazeData.rawCoordinates.x,gazeData.rawCoordinates.y)); //log.info(GazeEvent.GAZE_ENTERED + " : " +
     * gi.getNode()); } } else{//gaze is not on the shape
     * 
     * if(gi.isOn()){//gaze was on the shape previously
     * 
     * gi.setOn(false); gi.setTime(-1); gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(),
     * gazeData.rawCoordinates.x,gazeData.rawCoordinates.y)); //log.info(GazeEvent.GAZE_EXITED + " : " + gi.getNode());
     * } else{//gaze was not on the shape previously //nothing to do
     * 
     * }
     * 
     * } }
     * 
     * }
     */
}
