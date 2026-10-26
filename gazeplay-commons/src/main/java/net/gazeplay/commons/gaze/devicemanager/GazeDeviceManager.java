package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.function.Supplier;

/**
 * Turns the positions of the eye tracker into {@link GazeEvent}s on the nodes of the games.
 * <p>
 * A game registers its nodes ({@link #addEventFilter(Node)}); for each gaze sample, every registered node under the
 * position receives GAZE_ENTERED, GAZE_MOVED or GAZE_EXITED, then the root of the game receives the event too (its
 * filters record the statistics). In mouse mode there is no sample: the games use the mouse events.
 */
public interface GazeDeviceManager {

    /**
     * Starts the eye tracker.
     *
     * @param currentScreenDimensionSupplier dimension of the screen showing GazePlay
     * @param currentScreenPositionSupplier  position of that screen in the desktop
     */
    void init(Supplier<Dimension2D> currentScreenDimensionSupplier, Supplier<Point2D> currentScreenPositionSupplier);

    /**
     * Stops the eye tracker.
     */
    void destroy();

    /**
     * Registers a node: it will receive the gaze events (added at the next sample).
     */
    void addEventFilter(Node gs);

    /**
     * Registers a node in a second list, kept for compatibility: the events are only dispatched to the nodes of {@link
     * #addEventFilter(Node)}.
     */
    void addEventHandler(Node gs);

    /**
     * Unregisters a node, and the children of a pane (removed at the next sample; a node that was under the position
     * receives GAZE_EXITED).
     */
    void removeEventFilter(Node gs);

    /**
     * Sets the root of the game, which receives every event after the registered nodes: its filters record the
     * statistics. Called by each game when its round starts.
     */
    void addStats(Stats stats);

    /**
     * Unregisters a node of {@link #addEventHandler(Node)}.
     */
    void removeEventHandler(Node gs);

    /**
     * Unregisters all the nodes.
     */
    void clear();

    /**
     * Dispatches a recorded sample, during the replay of a game.
     *
     * @param gazePositionOnScreen position in the scene of the replay
     * @param event                "gaze" or "mouse"
     */
    void onSavedMovementsUpdate(Point2D gazePositionOnScreen, String event);

    /**
     * In replay mode, the samples of the eye tracker are ignored: only the recorded samples are dispatched.
     */
    void setInReplayMode(boolean b);

    /**
     * Pauses the dispatch, e.g. while the configuration menu of the game screen is open: the nodes under the gaze
     * receive GAZE_EXITED at once (a fixation in progress does not end while the player changes the settings), then
     * no event is fired until the dispatch is resumed. To be called on the JavaFX thread.
     */
    void setPaused(boolean paused);
}
