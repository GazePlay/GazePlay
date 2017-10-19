package eyetrackersservers;

import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;

/**
 * Created by schwab on 05/08/2017.
 */
class GazeListener implements IGazeListener {

    public GazeListener(){
    }

    @Override
    public void onGazeUpdate(GazeData gazeData){

        System.out.println("gazedata = " + gazeData.rawCoordinates);
    }
}