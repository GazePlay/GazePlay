package net.gazeplay.commons.gaze.devicemanager;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.GazeMotionListener;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by schwab on 04/10/2017.
 */
@Slf4j
public abstract class AbstractGazeDeviceManager implements GazeDeviceManager {

    private final List<GazeMotionListener> gazeMotionListeners = new CopyOnWriteArrayList<>();

    @Getter
    private final List<GazeInfos> shapesEventFilter = new CopyOnWriteArrayList<>();

    @Getter
    private final List<GazeInfos> shapesEventHandler = new CopyOnWriteArrayList<>();

    public AbstractGazeDeviceManager() {

    }

    @Override
    public abstract void init();

    @Override
    public abstract void destroy();

    @Override
    public void addGazeMotionListener(GazeMotionListener listener) {
        this.gazeMotionListeners.add(listener);
    }

    @Override
    public void removeGazeMotionListener(GazeMotionListener listener) {
        this.gazeMotionListeners.remove(listener);
    }

    private void notifyAllGazeMotionListeners(Point2D position) {
        for (GazeMotionListener l : this.gazeMotionListeners) {
            l.gazeMoved(position);
        }
    }

    @Override
    public void addEventFilter(Node gs) {
        shapesEventFilter.add(new GazeInfos(gs));
        final int nodesEventFilterListSize = shapesEventFilter.size();
        log.info("nodesEventFilterListSize = {}", nodesEventFilterListSize);
    }

    @Override
    public void addEventHandler(Node gs) {
        shapesEventHandler.add(new GazeInfos(gs));
    }

    @Override
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

    @Override
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
    @Override
    public void clear() {
        shapesEventFilter.clear();
        shapesEventHandler.clear();
        gazeMotionListeners.clear();
    }

    void onGazeUpdate(Point2D gazePositionOnScreen) {
        // log.info("gazedata = " + gazePositionOnScreen);

        notifyAllGazeMotionListeners(gazePositionOnScreen);

        final double positionX = gazePositionOnScreen.getX();
        final double positionY = gazePositionOnScreen.getY();

        for (GazeInfos gi : shapesEventFilter) {
            final Node node = gi.getNode();

            Point2D localPosition = node.screenToLocal(positionX, positionY);

            // log.info("localPosition = " + localPosition);

            if (node.contains(localPosition)) {

                if (gi.isOn()) {
                    Platform.runLater(() -> node
                            .fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(), positionX, positionY)));

                    // log.info(GazeEvent.GAZE_MOVED + " : " + gi.getNode());
                } else {

                    gi.setOn(true);
                    gi.setTime((new Date()).getTime());
                    Platform.runLater(() -> node
                            .fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, gi.getTime(), positionX, positionY)));

                    // log.info(GazeEvent.GAZE_ENTERED + " : " + gi.getNode());
                }
            } else {// gaze is not on the shape

                if (gi.isOn()) {// gaze was on the shape previously

                    gi.setOn(false);
                    gi.setTime(-1);
                    Platform.runLater(() -> node
                            .fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), positionX, positionY)));
                    // log.info(GazeEvent.GAZE_EXITED + " : " + gi.getNode());
                } else {// gaze was not on the shape previously
                    // nothing to do

                }

            }
        }

    }
}
