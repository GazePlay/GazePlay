package net.gazeplay.commons.gaze;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 16/08/2016.
 */
@Slf4j
public class GazeUtils {

    @Getter
    private static final GazeUtils instance = new GazeUtils();

    private final List<GazeInfos> nodesEventFilter;

    private final List<GazeInfos> nodesEventHandler;

    private final GazeManager gm;

    private final boolean success;

    private final IGazeListener gazeListener;

    @Getter
    private Stats stats;

    private Scene scene;

    private GazeUtils() {
        nodesEventFilter = new ArrayList<>(100);
        nodesEventHandler = new ArrayList<>(100);
        gm = GazeManager.getInstance();
        success = gm.activate();
        gazeListener = createGazeListener();
    }

    private IGazeListener createGazeListener() {

        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        final String eyetracker = config.getEyetracker();
        log.info("Eye-tracker = " + eyetracker);

        if (eyetracker.equals(EyeTracker.tobii_eyeX_4C.toString())) {
            GazeTobii.getInstance().execProg(new TobiiGazeListener(nodesEventFilter, nodesEventHandler));
        } else if (eyetracker.equals(EyeTracker.eyetribe.toString()))
            return new EyeTribeGazeListener(nodesEventFilter, nodesEventHandler);
        // else
        // return new FuzzyGazeListener(nodesEventFilter, nodesEventHandler);
        return null;
    }

    public void addStats(Stats newStats) {
        stats = newStats;
    }

    public void addEventFilter(Scene gazeScene) {
        scene = gazeScene;
    }

    public void addEventFilter(Node gs) {
        gm.addGazeListener(gazeListener);
        final int listenersCount = gm.getNumGazeListeners();
        log.info("Gaze Event Filters Count = {}", listenersCount);

        nodesEventFilter.add(new GazeInfos(gs));
        final int nodesEventFilterListSize = nodesEventFilter.size();
        log.info("nodesEventFilterListSize = {}", nodesEventFilterListSize);
    }

    public void addEventHandler(Node gs) {
        gm.addGazeListener(gazeListener);
        nodesEventHandler.add(new GazeInfos(gs));
    }

    public void removeEventFilter(Node gs) {

        int i;

        try {
            for (i = 0; i < nodesEventFilter.size() && nodesEventFilter.get(i).getNode() != null
                    && !nodesEventFilter.get(i).getNode().equals(gs); i++)
                ;

            if (i < nodesEventFilter.size()) {

                nodesEventFilter.remove(i);
            }
        } catch (Exception e) {

            log.debug(e.getMessage());
            System.exit(0);
        }
    }

    public void removeEventHandler(Node gs) {

        int i;

        for (i = 0; i < nodesEventHandler.size() && !nodesEventHandler.get(i).getNode().equals(gs); i++)
            ;

        if (i < nodesEventHandler.size()) {

            nodesEventHandler.remove(i);
        }
    }

    /**
     * Clear all Nodes in both EventFilter and EventHandler. There is no more gaze event after this function is called
     */
    public void clear() {

        nodesEventFilter.clear();

        nodesEventHandler.clear();
    }

    public boolean isOn() {
        return GazeTobii.getInstance().isInit() || success;
    }
}
