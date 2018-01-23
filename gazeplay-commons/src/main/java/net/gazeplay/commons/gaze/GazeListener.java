package net.gazeplay.commons.gaze;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by schwab on 04/10/2017.
 */
@Slf4j
public abstract class GazeListener {

    private final SecondScreen secondScreen;

    @Getter
    private final List<GazeInfos> shapesEventFilter = new ArrayList<>();

    @Getter
    private final List<GazeInfos> shapesEventHandler = new ArrayList<>();

    @Getter
    private Stats stats;

    public GazeListener() {
        this(null);
    }

    public GazeListener(SecondScreen secondScreen) {
        this.secondScreen = secondScreen;
    }

    public void addStats(Stats newStats) {
        stats = newStats;
    }

    public void addEventFilter(Node gs) {
        shapesEventFilter.add(new GazeInfos(gs));
        final int nodesEventFilterListSize = shapesEventFilter.size();
        log.info("nodesEventFilterListSize = {}", nodesEventFilterListSize);
    }

    public void addEventHandler(Node gs) {
        shapesEventHandler.add(new GazeInfos(gs));
    }

    public void removeEventFilter(Node gs) {
        int i;

        try {
            for (i = 0; i < shapesEventFilter.size() && shapesEventFilter.get(i).getNode() != null
                    && !shapesEventFilter.get(i).getNode().equals(gs); i++)
                ;

            if (i < shapesEventFilter.size()) {

                shapesEventFilter.remove(i);
            }
        } catch (Exception e) {

            log.debug(e.getMessage());
            System.exit(0);
        }
    }

    public void removeEventHandler(Node gs) {
        int i;

        for (i = 0; i < shapesEventHandler.size() && !shapesEventHandler.get(i).getNode().equals(gs); i++)
            ;

        if (i < shapesEventHandler.size()) {
            shapesEventHandler.remove(i);
        }
    }

    /**
     * Clear all Nodes in both EventFilter and EventHandler. There is no more gaze event after this function is called
     */
    public void clear() {
        shapesEventFilter.clear();
        shapesEventHandler.clear();
    }

    public void onGazeUpdate(Point2D gazePosition) {

        // log.info("gazedata = " + gazePosition);

        if (secondScreen != null) {
            secondScreen.light(gazePosition);
        }

        Stats stats = this.stats;
        if (stats != null) {
            stats.incHeatMap((int) gazePosition.getX(), (int) gazePosition.getY());
        }

        for (GazeInfos gi : shapesEventFilter) {

            javafx.geometry.Point2D p = gi.getNode().sceneToLocal(gazePosition.getX(), gazePosition.getY());

            // log.info("p = " + p);

            if (gi.getNode().contains(p)) {

                if (gi.isOn()) {

                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(), gazePosition.getX(),
                            gazePosition.getY()));
                    // log.info(GazeEvent.GAZE_MOVED + " : " + gi.getNode());
                } else {

                    gi.setOn(true);
                    gi.setTime((new Date()).getTime());
                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, gi.getTime(), gazePosition.getX(),
                            gazePosition.getY()));
                    // log.info(GazeEvent.GAZE_ENTERED + " : " + gi.getNode());
                }
            } else {// gaze is not on the shape

                if (gi.isOn()) {// gaze was on the shape previously

                    gi.setOn(false);
                    gi.setTime(-1);
                    gi.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), gazePosition.getX(),
                            gazePosition.getY()));
                    // log.info(GazeEvent.GAZE_EXITED + " : " + gi.getNode());
                } else {// gaze was not on the shape previously
                    // nothing to do

                }

            }
        }

    }
}
