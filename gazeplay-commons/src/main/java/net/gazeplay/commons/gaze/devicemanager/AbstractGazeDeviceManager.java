package net.gazeplay.commons.gaze.devicemanager;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.GazeMotionListener;
import net.gazeplay.commons.utils.ImmutableCachingSupplier;
import net.gazeplay.commons.utils.RobotSupplier;
import net.gazeplay.commons.utils.stats.Stats;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Created by schwab on 04/10/2017.
 */
@Slf4j
public abstract class AbstractGazeDeviceManager implements GazeDeviceManager {

    private boolean isInReplayMode = false;

    private final List<GazeMotionListener> gazeMotionListeners = new CopyOnWriteArrayList<>();

    @Getter
    private final Map<IdentityKey<Node>, GazeInfos> shapesEventFilter = Collections.synchronizedMap(new HashMap<>());

    @Getter
    private final Map<IdentityKey<Node>, GazeInfos> shapesEventHandler = Collections.synchronizedMap(new HashMap<>());

    private final List<Node> toRemove = new LinkedList<>();
    private final List<Node> toAdd = new LinkedList<>();

    private final Supplier<Robot> robotSupplier = new ImmutableCachingSupplier<>(new RobotSupplier());

    public AbstractGazeDeviceManager() {

    }

    @Override
    public abstract void init(Supplier<Dimension2D> currentScreenDimensionSupplier, Supplier<Point2D> currentScreenPositionSupplier);

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
        toAdd.add(gs);
    }

    @Override
    public void setInReplayMode(boolean b) {
        isInReplayMode = b;
    }

    public void add() {
        synchronized (shapesEventFilter) {
            List<Node> temp = new LinkedList<>(toAdd);
            for (Node node : temp) {
                shapesEventFilter.put(new IdentityKey<>(node), new GazeInfos(node));
                toAdd.remove(node);
            }
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
        toRemove.add(gs);
        if (gs instanceof Pane) {
            for (Node child : ((Pane) gs).getChildren()) {
                removeEventFilter(child);
            }
        }
    }


    public GazeInfos gameScene = null;

    public void addStats(Stats stats) {
        gameScene = new GazeInfos(stats.gameContextScene.getRoot());
    }

    public void delete() {
        synchronized (shapesEventFilter) {
            List<Node> temp = new LinkedList<>(toRemove);
            for (Node node : temp) {
                GazeInfos removed = shapesEventFilter.remove(new IdentityKey<>(node));
                if (removed == null) {
                    log.warn("EventFilter to remove not found");
                } else {
                    if (removed.isOnGaze() || removed.isOnMouse()) {
                        Platform.runLater(
                            () ->
                                removed.getNode().fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, System.currentTimeMillis(), 0, 0))
                        );
                    }
                }
                toRemove.remove(node);
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

    synchronized void onGazeUpdate(Point2D gazePositionOnScreen, String event) {
        notifyAllGazeMotionListeners(gazePositionOnScreen);
        final double positionX = gazePositionOnScreen.getX();
        final double positionY = gazePositionOnScreen.getY();
        updatePosition(positionX, positionY, event, false);
    }

    @Override
    synchronized public void onSavedMovementsUpdate(Point2D gazePositionOnScene, String event) {
        if (gameScene != null) {
            Point2D gazePositionOnScreen = gameScene.getNode().localToScreen(gazePositionOnScene);
            if (gazePositionOnScreen != null) {
                final double positionX = gazePositionOnScreen.getX();
                final double positionY = gazePositionOnScreen.getY();
                updatePosition(positionX, positionY, event, true);
            }
        }
    }

    void updatePosition(double positionX, double positionY, String event, Boolean isBeingReplayed) {

        if (!isInReplayMode || (isInReplayMode && isBeingReplayed)) {

            add();
            delete();

            synchronized (shapesEventFilter) {
                Collection<GazeInfos> c = shapesEventFilter.values();
                for (GazeInfos gi : c) {
                    if (gameScene != null && gi.getNode() != gameScene.getNode()) {
                        eventFire(positionX, positionY, gi, event, c);
                    }
                }

                if (gameScene != null) {
                    eventFire(positionX, positionY, gameScene, event);
                }

            }
        }
    }

    public boolean contains(Node node, double positionX, double positionY) {
        Point2D localPosition = node.screenToLocal(positionX, positionY);
        if (localPosition != null) {
            return node.contains(localPosition.getX(), localPosition.getY());
        }
        return false;
    }


    public void eventFire(double positionX, double positionY, GazeInfos gi, String event) {
        eventFire(positionX, positionY, gi, event, null);
    }

    public boolean eventFire(double positionX, double positionY, GazeInfos gi, String event, Collection<GazeInfos> c) {
        Node node = gi.getNode();
        if (!node.isDisable()) {

            Point2D localPosition = node.screenToLocal(positionX, positionY);

            if (localPosition != null && contains(node, positionX, positionY)) {
                if (event.equals("gaze")) {
                    if (gi.isOnGaze()) {
                        Platform.runLater(
                            () ->
                                node.fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(), localPosition.getX(), localPosition.getY()))
                        );
                        return true;
                    } else {

                        gi.setOnGaze(true);
                        gi.setTime(System.currentTimeMillis());
                        Platform.runLater(
                            () ->
                                node.fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, gi.getTime(), localPosition.getX(), localPosition.getY()))
                        );
                        return true;
                    }
                } else {
                    if (gi.isOnMouse()) {
                        Platform.runLater(
                            () ->
                                node.fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(), localPosition.getX(), localPosition.getY()))

                        );
                        return true;
                    } else {

                        gi.setOnMouse(true);
                        gi.setTime(System.currentTimeMillis());
                        Platform.runLater(
                            () ->
                                node.fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, gi.getTime(), localPosition.getX(), localPosition.getY()))

                        );
                        return true;
                    }
                }
            } else {// gaze is not on the shape
                if (event.equals("gaze")) {
                    if (gi.isOnGaze()) {// gaze was on the shape previously
                        gi.setOnGaze(false);
                        gi.setTime(-1);
                        if (localPosition != null) {
                            Platform.runLater(
                                () ->
                                    node.fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), localPosition.getX(), localPosition.getY()))
                            );
                        }
                    }
                } else {
                    if (gi.isOnMouse()) {// gaze was on the shape previously
                        gi.setOnMouse(false);
                        gi.setTime(-1);
                        if (localPosition != null) {
                            Platform.runLater(
                                () ->
                                    node.fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), localPosition.getX(), localPosition.getY()))

                            );
                        }
                    }
                }

            }
        }
        return false;
    }

}
