package net.gazeplay.commons.gaze.devicemanager;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.List;
import java.util.*;
import java.util.function.Supplier;

/**
 * Dispatch of the gaze samples to the registered nodes (see {@link GazeDeviceManager}), whatever their source.
 * <p>
 * The nodes added or removed by a game are only applied at the next sample ({@link #add()}, {@link #delete()}), so that
 * a game can change them from an event handler during a dispatch.
 * <p>
 * Created by schwab on 04/10/2017.
 */
@Slf4j
public abstract class AbstractGazeDeviceManager implements GazeDeviceManager {

    private boolean isInReplayMode = false;

    /**
     * See {@link #setPaused(boolean)}. Set and read on the JavaFX thread.
     */
    private boolean paused = false;

    // Also the lock of the dispatch: not exposed (it had unused public getters)
    private final Map<IdentityKey<Node>, GazeInfos> shapesEventFilter = Collections.synchronizedMap(new HashMap<>());

    private final Map<IdentityKey<Node>, GazeInfos> shapesEventHandler = Collections.synchronizedMap(new HashMap<>());

    private final List<Node> toRemove = new LinkedList<>();
    private final List<Node> toAdd = new LinkedList<>();

    public AbstractGazeDeviceManager() {

    }

    @Override
    public abstract void init(Supplier<Dimension2D> currentScreenDimensionSupplier, Supplier<Point2D> currentScreenPositionSupplier);

    @Override
    public abstract void destroy();

    @Override
    public void addEventFilter(Node gs) {
        toAdd.add(gs);
    }

    @Override
    public void setInReplayMode(boolean b) {
        isInReplayMode = b;
    }

    @Override
    public void setPaused(final boolean paused) {
        this.paused = paused;
        if (!paused) {
            return;
        }
        final List<GazeInfos> nodes;
        synchronized (shapesEventFilter) {
            nodes = new ArrayList<>(shapesEventFilter.values());
        }
        if (gameScene != null) {
            nodes.add(gameScene);
        }
        for (GazeInfos gi : nodes) {
            if (gi.isOnGaze() || gi.isOnMouse()) {
                gi.setOnGaze(false);
                gi.setOnMouse(false);
                gi.setTime(-1);
                fire(gi.getNode(), new GazeEvent(GazeEvent.GAZE_EXITED, -1, 0, 0));
            }
        }
    }

    /**
     * Applies the pending additions of nodes.
     */
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


    private GazeInfos gameScene = null;

    public void addStats(Stats stats) {
        gameScene = new GazeInfos(stats.gameContextScene.getRoot());
    }

    /**
     * Applies the pending removals of nodes; a node that was under the gaze or the mouse receives GAZE_EXITED.
     */
    public void delete() {
        synchronized (shapesEventFilter) {
            List<Node> temp = new LinkedList<>(toRemove);
            for (Node node : temp) {
                GazeInfos removed = shapesEventFilter.remove(new IdentityKey<>(node));
                if (removed == null) {
                    log.warn("EventFilter to remove not found");
                } else {
                    if (removed.isOnGaze() || removed.isOnMouse()) {
                        fire(removed.getNode(), new GazeEvent(GazeEvent.GAZE_EXITED, System.currentTimeMillis(), 0, 0));
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
        }
    }

    /**
     * Single entry point of the positions given by the gaze sources: the dispatch to the nodes runs on the JavaFX
     * thread, reached once per sample. It used to be reached once per node and per sample (one Platform.runLater for
     * each event), and the positions of the former EyeTribe eye trackers were dispatched on the thread of their SDK,
     * which read the JavaFX nodes outside of the JavaFX thread.
     */
    public void onGazeSample(final GazeSample sample) {
        if (Platform.isFxApplicationThread()) {
            dispatch(sample, false);
        } else {
            Platform.runLater(() -> dispatch(sample, false));
        }
    }

    void onGazeUpdate(Point2D gazePositionOnScreen, String event) {
        onGazeSample(GazeSample.now(gazePositionOnScreen, GazeSample.Source.fromEventName(event)));
    }

    /**
     * Replay of the recorded samples, on the JavaFX thread (the replay draws them on a canvas).
     */
    @Override
    public void onSavedMovementsUpdate(Point2D gazePositionOnScene, String event) {
        if (gameScene != null) {
            Point2D gazePositionOnScreen = gameScene.getNode().localToScreen(gazePositionOnScene);
            if (gazePositionOnScreen != null) {
                dispatch(GazeSample.now(gazePositionOnScreen, GazeSample.Source.fromEventName(event)), true);
            }
        }
    }

    /**
     * Fires the events of a sample on the registered nodes, then on the root of the game (whose filters record the
     * statistics). The events are fired directly, on a copy of the registered nodes: a handler may add, remove or clear
     * the nodes during the dispatch (e.g. a game ending on GAZE_ENTERED) without affecting it, as when the events were
     * queued with Platform.runLater (pinned by GazeDispatchCharacterizationTest).
     */
    private void dispatch(final GazeSample sample, final boolean isBeingReplayed) {
        if ((isInReplayMode || paused) && !isBeingReplayed) {
            return;
        }

        add();
        delete();

        final GazeInfos root = gameScene;
        if (root == null) {
            return;
        }
        final List<GazeInfos> nodes;
        synchronized (shapesEventFilter) {
            nodes = new ArrayList<>(shapesEventFilter.values());
        }

        final double positionX = sample.screenPosition().getX();
        final double positionY = sample.screenPosition().getY();
        final String event = sample.source().eventName();
        for (GazeInfos gi : nodes) {
            if (gi.getNode() != root.getNode()) {
                eventFire(positionX, positionY, gi, event, sample.time());
            }
        }
        eventFire(positionX, positionY, root, event, sample.time());
    }

    /**
     * @return whether a position of the screen is on the node
     */
    public boolean contains(Node node, double positionX, double positionY) {
        Point2D localPosition = node.screenToLocal(positionX, positionY);
        if (localPosition != null) {
            return node.contains(localPosition.getX(), localPosition.getY());
        }
        return false;
    }

    private static void fire(final Node node, final GazeEvent event) {
        node.fireEvent(event);
    }

    /**
     * Updates the gaze (or mouse) state of a node for a position and fires the matching event: GAZE_ENTERED, then
     * GAZE_MOVED with the time of the entry, then GAZE_EXITED with the time -1. Gaze and mouse have separate states.
     *
     * @return true if the position is on the node
     */
    public boolean eventFire(double positionX, double positionY, GazeInfos gi, String event) {
        return eventFire(positionX, positionY, gi, event, 0);
    }

    /**
     * @param sampleTime acquisition time of the sample, given to the events ({@link GazeEvent#getSampleTime()})
     */
    boolean eventFire(double positionX, double positionY, GazeInfos gi, String event, long sampleTime) {
        final Node node = gi.getNode();
        if (node.isDisable()) {
            return false;
        }
        final boolean gaze = event.equals("gaze");
        final boolean wasOn = gaze ? gi.isOnGaze() : gi.isOnMouse();
        final Point2D localPosition = node.screenToLocal(positionX, positionY);

        if (localPosition != null && contains(node, positionX, positionY)) {
            if (!wasOn) {
                if (gaze) {
                    gi.setOnGaze(true);
                } else {
                    gi.setOnMouse(true);
                }
                gi.setTime(System.currentTimeMillis());
            }
            fire(node, new GazeEvent(wasOn ? GazeEvent.GAZE_MOVED : GazeEvent.GAZE_ENTERED, gi.getTime(),
                localPosition.getX(), localPosition.getY(), sampleTime));
            return true;
        }

        if (wasOn) { // the position left the node
            if (gaze) {
                gi.setOnGaze(false);
            } else {
                gi.setOnMouse(false);
            }
            gi.setTime(-1);
            if (localPosition != null) {
                fire(node, new GazeEvent(GazeEvent.GAZE_EXITED, gi.getTime(), localPosition.getX(), localPosition.getY(),
                    sampleTime));
            }
        }
        return false;
    }

}
