package gaze;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import gaze.configuration.Configuration;
import javafx.scene.Node;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import tobii.Tobii;
import net.gazeplay.utils.stats.Stats;

import java.util.ArrayList;

/**
 * Created by schwab on 16/08/2016.
 */
@Slf4j
public class GazeUtils {

    static ArrayList<GazeInfos> nodesEventFilter = new ArrayList<GazeInfos>(100);

    static ArrayList<GazeInfos> nodesEventHandler = new ArrayList<GazeInfos>(100);

    static final GazeManager gm = GazeManager.getInstance();
    static boolean success = gm.activate();
    static final IGazeListener gazeListener = createGazeListener();

    static Stats stats;

    static Scene scene = null;

    private static IGazeListener createGazeListener() {

        Configuration config = new Configuration();

        log.info("Eye-tracker = " + config.eyetracker);

        if (config.eyetracker.equals("tobii")) {

            Tobii.execProg(new TobiiGazeListener(nodesEventFilter, nodesEventHandler));
        } else if (config.gazeMode.equals("true"))
            return new EyeTribeGazeListener(nodesEventFilter, nodesEventHandler);
        else
            return new FuzzyGazeListener(nodesEventFilter, nodesEventHandler);
        return null;
    }

    public static void addStats(Stats newStats) {

        stats = newStats;
    }

    public static void addEventFilter(Scene gazeScene) {

        scene = gazeScene;
    }

    public static void addEventFilter(Node gs) {

        gm.addGazeListener(gazeListener);

        nodesEventFilter.add(new GazeInfos(gs));
    }

    public static void addEventHandler(Node gs) {

        gm.addGazeListener(gazeListener);

        nodesEventHandler.add(new GazeInfos(gs));
    }

    public static void removeEventFilter(Node gs) {

        int i;

        try {
            for (i = 0; i < nodesEventFilter.size() && nodesEventFilter.get(i).getNode() != null
                    && !nodesEventFilter.get(i).getNode().equals(gs); i++)
                ;

            if (i < nodesEventFilter.size()) {

                nodesEventFilter.remove(i);
            }
        } catch (Exception e) {

        }
    }

    public static void removeEventHandler(Node gs) {

        int i;

        for (i = 0; i < nodesEventHandler.size() && !nodesEventHandler.get(i).getNode().equals(gs); i++)
            ;

        if (i < nodesEventHandler.size()) {

            nodesEventHandler.remove(i);
        }
    }

    public static boolean isOn() {
        return Tobii.isInit() || success;
    }
}
