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
    public abstract void init(Supplier<Dimension2D> currentScreenDimensionSupplier);

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

    public void delete() {
        synchronized (shapesEventFilter) {
            List<Node> temp = new LinkedList<>(toRemove);
            for (Node node : temp) {
                GazeInfos removed = shapesEventFilter.remove(new IdentityKey<>(node));
                if (removed == null) {
                    log.warn("EventFilter to remove not found");
                } else {
                    if (removed.isOn()) {
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

    synchronized void onGazeUpdate(Point2D gazePositionOnScreen) {

        // notifyAllGazeMotionListeners(gazePositionOnScreen);
        final double positionX = gazePositionOnScreen.getX();
        final double positionY = gazePositionOnScreen.getY();

        Configuration config = ActiveConfigurationContext.getInstance();

        if (config.isGazeMouseEnable() && !config.isMouseFree()) {
            Platform.runLater(
                () -> robotSupplier.get().mouseMove((int) positionX, (int) positionY)
            );
        }

        add();
        delete();

        Collection<GazeInfos> c = shapesEventFilter.values();

        synchronized (shapesEventFilter) {

            for (GazeInfos gi : c) {
                final Node node = gi.getNode();

                eventFire(positionX, positionY, gi, node);
                // log.info("Fire : "+node+" then recursion !");
                recursiveEventFire(positionX, positionY, node);

            }

        }
    }

    public void recursiveEventFire(double positionX, double positionY, Node node) {
        if (node instanceof Pane) {
            for (Node child : ((Pane) node).getChildren()) {
                if (!shapesEventFilter.containsKey(new IdentityKey<>(child))) {
                    // log.info("child : "+child+" added !");
                    addEventFilter(child);
                }
                // log.info("child : "+child+" fired !");
                GazeInfos gi = shapesEventFilter.get(new IdentityKey<>(child));
                if (gi != null) {
                    eventFire(positionX, positionY, gi, child);
                }
                recursiveEventFire(positionX, positionY, child);

            }
        }
    }

    public void eventFire(double positionX, double positionY, GazeInfos gi, Node node) {
        // log.info("GazeInfo: " + gi);
        if (!node.isDisable()) {

            Point2D localPosition = node.screenToLocal(positionX, positionY);
            if (localPosition != null && node.contains(localPosition)) {
                if (gi.isOn()) {
                    Platform.runLater(
                        () ->
                            node.fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, gi.getTime(), positionX, positionY))
                    );
                } else {

                    gi.setOn(true);
                    gi.setTime(System.currentTimeMillis());
                    Platform.runLater(
                        () ->
                            node.fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, gi.getTime(), positionX, positionY))
                    );
                }
            } else {// gaze is not on the shape

                if (gi.isOn()) {// gaze was on the shape previously

                    gi.setOn(false);
                    gi.setTime(-1);
                    Platform.runLater(
                        () ->
                            node.fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), positionX, positionY))
                    );
                } else {// gaze was not on the shape previously
                    // nothing to do

                }

            }
        }
    }

}
