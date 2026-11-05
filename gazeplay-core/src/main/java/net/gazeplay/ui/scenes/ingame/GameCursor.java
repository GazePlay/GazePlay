package net.gazeplay.ui.scenes.ingame;

import javafx.animation.PauseTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.LongSupplier;

/**
 * The mouse pointer on a game screen, games and evaluations (decisions of the maintainer, 5/11/2026), so that it does not
 * distract a player using the eye tracker. It is always visible with the mouse alone (no {@code GameCursor} then), in the
 * menu of the game (which stops the mouse events outside of it before the filters of the scene, so they could not bring it
 * back) and out of the game screens.
 * <ul>
 * <li>{@link #forGazeOnly}: the mouse does not play. The pointer is hidden, and comes back while the mouse moves;</li>
 * <li>{@link #forLastInput}: both play, and the pointer follows the last input used. The gaze never stops coming, even
 * while the mouse is used: the mouse keeps the lead for a while after it moved, long enough to select something with a still
 * mouse, otherwise the pointer would flicker and vanish during a fixation. Without gaze, the pointer stays. Once hidden, the
 * mouse no longer plays: something passing under it does not start a fixation. It leaves what it was on, and comes back on
 * what is under it when it moves.</li>
 * </ul>
 */
final class GameCursor {

    private final Scene scene;
    /** Gaze only: brings the pointer down after the mouse stopped. */
    private final PauseTransition idle;
    /** Both inputs: how long the mouse keeps the lead after it moved. */
    private final LongSupplier mouseLeadMillis;
    private final EventHandler<MouseEvent> onMouseActivity = event -> mouseUsed(event);
    private final EventHandler<MouseEvent> onMouseCrossing = this::trackWhatIsUnderTheMouse;
    /** The nodes under the mouse, the outermost first, as JavaFX enters them; also while the mouse is out of the game. */
    private final Set<Node> underTheMouse = new LinkedHashSet<>();
    private Cursor cursorBefore;
    private boolean running;
    private boolean menuOpen;
    private boolean mouseOutOfTheGame;
    private boolean replayingCrossings;
    private long lastMouseUse = Long.MIN_VALUE;
    private double lastMouseX;
    private double lastMouseY;

    private GameCursor(Scene scene, Duration idleTime, LongSupplier mouseLeadMillis) {
        this.scene = scene;
        this.mouseLeadMillis = mouseLeadMillis;
        this.idle = new PauseTransition(idleTime);
        idle.setOnFinished(event -> scene.setCursor(Cursor.NONE));
    }

    /**
     * @param idleTime how long the mouse stays still before its pointer is hidden again
     */
    static GameCursor forGazeOnly(Scene scene, Duration idleTime) {
        return new GameCursor(scene, idleTime, null);
    }

    /**
     * @param mouseLeadMillis how long the mouse keeps the lead after it moved, read each time
     */
    static GameCursor forLastInput(Scene scene, LongSupplier mouseLeadMillis) {
        return new GameCursor(scene, Duration.ZERO, mouseLeadMillis);
    }

    private boolean followsLastInput() {
        return mouseLeadMillis != null;
    }

    /**
     * The game screen is shown.
     */
    void start() {
        if (running) {
            return;
        }
        running = true;
        cursorBefore = scene.getCursor();
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, onMouseActivity);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, onMouseActivity);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, onMouseActivity);
        scene.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, onMouseCrossing);
        scene.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, onMouseCrossing);
        // With both inputs, nothing is known yet: the pointer goes at the first position of the gaze
        if (!menuOpen && !followsLastInput()) {
            scene.setCursor(Cursor.NONE);
        }
    }

    /**
     * Another screen is shown: gives the pointer back.
     */
    void stop() {
        if (!running) {
            return;
        }
        running = false;
        idle.stop();
        scene.removeEventFilter(MouseEvent.MOUSE_MOVED, onMouseActivity);
        scene.removeEventFilter(MouseEvent.MOUSE_DRAGGED, onMouseActivity);
        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, onMouseActivity);
        scene.removeEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, onMouseCrossing);
        scene.removeEventFilter(MouseEvent.MOUSE_EXITED_TARGET, onMouseCrossing);
        underTheMouse.clear();
        mouseOutOfTheGame = false;
        scene.setCursor(cursorBefore);
    }

    /**
     * Follows the menu of the game: the pointer stays visible while it is open.
     */
    void setMenuOpen(boolean open) {
        menuOpen = open;
        if (!running) {
            return;
        }
        if (open) {
            idle.stop();
            scene.setCursor(cursorBefore);
        } else if (mouseOutOfTheGame) {
            scene.setCursor(Cursor.NONE);
        } else if (!followsLastInput()) {
            idle.playFromStart();
        }
    }

    /**
     * A new position of the gaze.
     */
    void gazeUsed() {
        if (running && !menuOpen && followsLastInput() && !mouseOutOfTheGame && !mouseHasTheLead()) {
            scene.setCursor(Cursor.NONE);
            List<Node> innermostFirst = new ArrayList<>(underTheMouse);
            Collections.reverse(innermostFirst);
            replayCrossings(innermostFirst, MouseEvent.MOUSE_EXITED_TARGET);
            mouseOutOfTheGame = true;
        }
    }

    /**
     * @return whether the mouse events must not reach the game
     */
    boolean isMouseOutOfTheGame() {
        return mouseOutOfTheGame;
    }

    private boolean mouseHasTheLead() {
        return lastMouseUse != Long.MIN_VALUE && (System.nanoTime() - lastMouseUse) / 1_000_000 < mouseLeadMillis.getAsLong();
    }

    private void mouseUsed(MouseEvent event) {
        lastMouseUse = System.nanoTime();
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
        scene.setCursor(cursorBefore);
        if (mouseOutOfTheGame) {
            mouseOutOfTheGame = false;
            replayCrossings(new ArrayList<>(underTheMouse), MouseEvent.MOUSE_ENTERED_TARGET);
        }
        if (!menuOpen && !followsLastInput()) {
            idle.playFromStart();
        }
    }

    private void trackWhatIsUnderTheMouse(MouseEvent event) {
        if (replayingCrossings || !(event.getTarget() instanceof Node node)) {
            return;
        }
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
        if (event.getEventType() == MouseEvent.MOUSE_ENTERED_TARGET) {
            underTheMouse.add(node);
        } else {
            underTheMouse.remove(node);
        }
    }

    /**
     * Tells the nodes that the mouse leaves them or comes back on them, as it is not JavaFX that decides it here: for
     * JavaFX, the mouse has not moved.
     */
    private void replayCrossings(List<Node> nodes, EventType<MouseEvent> type) {
        replayingCrossings = true;
        for (Node node : nodes) {
            if (node.getScene() == scene) {
                Event.fireEvent(node, new MouseEvent(type, lastMouseX, lastMouseY, lastMouseX, lastMouseY, MouseButton.NONE,
                    0, false, false, false, false, false, false, false, false, false, false, null));
            }
        }
        replayingCrossings = false;
    }
}
