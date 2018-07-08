package net.gazeplay.commons.gaze.devicemanager;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.GazeMotionListener;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by schwab on 04/10/2017.
 */
@Slf4j
public abstract class AbstractGazeDeviceManager implements GazeDeviceManager {

    private final List<GazeMotionListener> gazeMotionListeners = new CopyOnWriteArrayList<>();

    @Getter
    private final Map<IdentityKey<Node>, GazeInfos> shapesEventFilter = Collections
            .synchronizedMap(new HashMap<IdentityKey<Node>, GazeInfos>());

    @Getter
    private final Map<IdentityKey<Node>, GazeInfos> shapesEventHandler = Collections
            .synchronizedMap(new HashMap<IdentityKey<Node>, GazeInfos>());

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
        synchronized (shapesEventFilter) {
            shapesEventFilter.put(new IdentityKey<>(gs), new GazeInfos(gs));
            final int nodesEventFilterListSize = shapesEventFilter.size();
            // log.info("nodesEventFilterListSize = {}", nodesEventFilterListSize);
        }
    }

    @Override
    public void addEventHandler(Node gs) {
        synchronized (shapesEventFilter) {
            shapesEventHandler.put(new IdentityKey<>(gs), new GazeInfos(gs));
        }
    }

    @Override
    public void removeEventFilter(Node gs) {
        synchronized (shapesEventFilter) {
            GazeInfos removed = shapesEventFilter.remove(new IdentityKey<>(gs));
            if (removed == null) {
                log.warn("EventFilter to remove not found");
            } else {
                if (removed.isOn()) {
                    Platform.runLater(() -> removed.getNode()
                            .fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, System.currentTimeMillis(), 0, 0)));
                }
            }
        }
    }

    @Override
    public void removeEventHandler(Node gs) {
        synchronized (shapesEventFilter) {
            GazeInfos removed = shapesEventHandler.remove(new IdentityKey<>(gs));
            if (removed == null) {
                log.warn("EventHandler to remove not found");
            }
        }
    }

    /**
     * Clear all Nodes in both EventFilter and EventHandler. There is no more gaze event after this function is called
     */
    @Override
    public void clear() {
        synchronized (shapesEventFilter) {
            shapesEventFilter.clear();
            shapesEventHandler.clear();
            gazeMotionListeners.clear();
        }
    }

    synchronized void onGazeUpdate(Point2D gazePositionOnScreen) {
        // log.info("gazedata = " + gazePositionOnScreen);

        notifyAllGazeMotionListeners(gazePositionOnScreen);

        final double positionX = gazePositionOnScreen.getX();
        final double positionY = gazePositionOnScreen.getY();

        Collection<GazeInfos> c = shapesEventFilter.values(); // Needn't be in synchronized block
        synchronized (shapesEventFilter) { // Synchronizing on m, not s!
            Iterator<GazeInfos> i = c.iterator(); // Must be in synchronized block
            while (i.hasNext()) {
                GazeInfos gi = i.next();

                final Node node = gi.getNode();

                // log.info("localPosition = " + localPosition);

                if (!node.isDisable()) {

                    Point2D localPosition = node.screenToLocal(positionX, positionY);

                    if (localPosition != null && node.contains(localPosition)) {
                        if (gi.isOn()) {
                            Platform.runLater(() -> node.fireEvent(
                                    new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(), positionX, positionY)));

                            // log.info(GazeEvent.GAZE_MOVED + " : " + gi.getNode());
                        } else {

                            gi.setOn(true);
                            gi.setTime(System.currentTimeMillis());
                            Platform.runLater(() -> node.fireEvent(
                                    new GazeEvent(GazeEvent.GAZE_ENTERED, gi.getTime(), positionX, positionY)));

                            // log.info(GazeEvent.GAZE_ENTERED + " : " + gi.getNode());
                        }
                    } else {// gaze is not on the shape

                        if (gi.isOn()) {// gaze was on the shape previously

                            gi.setOn(false);
                            gi.setTime(-1);
                            Platform.runLater(() -> node.fireEvent(
                                    new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), positionX, positionY)));
                            // log.info(GazeEvent.GAZE_EXITED + " : " + gi.getNode());
                        } else {// gaze was not on the shape previously
                            // nothing to do

                        }

                    }
                }
            }
        }
    }
}
