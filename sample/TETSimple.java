package sample;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.utils.GazeUtils;

/**
 * Created by schwab on 13/08/2016.
 */
public class TETSimple {

    public static void main(String[] args) {

        final GazeManager gm = GazeManager.getInstance();
        boolean success = gm.activate();
        final GazeListener gazeListener = new GazeListener();
        gm.addGazeListener(gazeListener);
    }
}

class GazeListener implements IGazeListener {

    @Override
    public void onGazeUpdate(GazeData gazeData)
    {
        System.out.println(GazeUtils.getEyesCenterNormalized(gazeData));
    }
}