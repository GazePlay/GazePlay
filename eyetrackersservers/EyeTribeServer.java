package eyetrackersservers;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;

public class EyeTribeServer {

    public static void main(String argv[]){

        final GazeManager gm = GazeManager.getInstance();
        System.out.println(gm.activate());
        final IGazeListener gazeListener = new GazeListener();
        gm.addGazeListener(gazeListener);
    }

}
