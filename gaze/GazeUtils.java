package gaze;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import gaze.Configuration.Configuration;
import javafx.scene.Node;

import java.util.ArrayList;

/**
 * Created by schwab on 16/08/2016.
 */
public class GazeUtils {

    static ArrayList<GazeInfos> nodesEventFilter = new ArrayList<GazeInfos>(100);

    static ArrayList<GazeInfos> nodesEventHandler = new ArrayList<GazeInfos>(100);


    static final GazeManager gm = GazeManager.getInstance();
    static boolean success = gm.activate();
    static final IGazeListener gazeListener = createGazeListener();//new TrueGazeListener(nodesEventFilter, nodesEventHandler);

    private static IGazeListener createGazeListener() {

        Configuration config = new Configuration();

        if(config.gazeMode.equals("true"))
            return new TrueGazeListener(nodesEventFilter, nodesEventHandler);
        else
            return  new FuzzyGazeListener(nodesEventFilter, nodesEventHandler);
    }

    public static void addEventFilter(Node gs){

        gm.addGazeListener(gazeListener);

        nodesEventFilter.add(new GazeInfos(gs));
    }

    public static void addEventHandler(Node gs){

        gm.addGazeListener(gazeListener);

        nodesEventHandler.add(new GazeInfos(gs));
    }

    public static void removeEventFilter(Node gs){

        int i;

        for(i = 0; i < nodesEventFilter.size() && ! nodesEventFilter.get(i).getNode().equals(gs); i++);

        if(i < nodesEventFilter.size()){

            nodesEventFilter.remove(i);
        }
    }

    public static void removeEventHandler(Node gs){

        int i;

        for(i = 0; i < nodesEventHandler.size() && ! nodesEventHandler.get(i).getNode().equals(gs); i++);

        if(i < nodesEventHandler.size()){

            nodesEventHandler.remove(i);
        }
    }
}
