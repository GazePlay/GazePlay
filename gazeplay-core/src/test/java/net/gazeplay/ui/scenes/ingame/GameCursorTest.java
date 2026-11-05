package net.gazeplay.ui.scenes.ingame;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The mouse pointer of a game screen (decisions of the maintainer of 5/11/2026): always visible with the mouse alone, hidden
 * unless it moves with the gaze alone, and following the last input used with both.
 */
@ExtendWith(ApplicationExtension.class)
class GameCursorTest {

    private static final long LONG_LEAD = 60_000;

    private Scene scene;
    private GameCursor cursor;
    private final Pane gameRoot = new Pane();
    private final AtomicLong mouseLead = new AtomicLong(LONG_LEAD);

    private void onFxThread(Runnable action) {
        WaitForAsyncUtils.waitForAsyncFx(5000, action);
    }

    private void gazeOnly(double idleMillis) {
        onFxThread(() -> {
            scene = new Scene(new Pane());
            cursor = GameCursor.forGazeOnly(scene, Duration.millis(idleMillis));
            cursor.start();
        });
    }

    private void bothInputs() {
        onFxThread(() -> {
            scene = new Scene(new Pane());
            cursor = GameCursor.forLastInput(scene, mouseLead::get);
            cursor.start();
        });
    }

    private void moveTheMouse() {
        onFxThread(() -> scene.getRoot().fireEvent(new MouseEvent(MouseEvent.MOUSE_MOVED, 5, 5, 5, 5, MouseButton.NONE, 0,
            false, false, false, false, false, false, false, false, false, false, null)));
    }

    @Test
    void shouldHideThePointerAtOnceWithTheGazeOnly() {
        gazeOnly(100);

        assertEquals(Cursor.NONE, scene.getCursor());
    }

    @Test
    void shouldShowThePointerWhileTheMouseMovesWithTheGazeOnly() throws TimeoutException {
        gazeOnly(100);

        moveTheMouse();
        assertNull(scene.getCursor());

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> scene.getCursor() == Cursor.NONE);
    }

    @Test
    void shouldIgnoreTheGazeWithTheGazeOnlyAsItIsAlwaysThere() {
        gazeOnly(60_000);

        moveTheMouse();
        onFxThread(() -> cursor.gazeUsed());

        assertNull(scene.getCursor());
    }

    /**
     * The menu of the game stops the mouse events outside of it: moving the mouse could not bring the pointer back.
     */
    @Test
    void shouldKeepThePointerWhileTheMenuIsOpen() throws Exception {
        gazeOnly(50);

        onFxThread(() -> cursor.setMenuOpen(true));
        assertNull(scene.getCursor());
        Thread.sleep(300);
        assertNull(scene.getCursor());

        onFxThread(() -> cursor.setMenuOpen(false));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> scene.getCursor() == Cursor.NONE);
    }

    @Test
    void shouldGiveThePointerBackWhenStopped() throws Exception {
        gazeOnly(50);

        onFxThread(() -> cursor.stop());
        assertNull(scene.getCursor());

        moveTheMouse();
        Thread.sleep(200);
        assertNull(scene.getCursor());
    }

    @Test
    void shouldKeepThePointerUntilTheGazeComesWithBothInputs() {
        bothInputs();

        assertNull(scene.getCursor());

        onFxThread(() -> cursor.gazeUsed());
        assertEquals(Cursor.NONE, scene.getCursor());
    }

    /**
     * The gaze never stops coming: right after the mouse moved, it must not take the pointer away, e.g. during the fixation
     * of a bubble selected with a still mouse (remark of the maintainer).
     */
    @Test
    void shouldLeaveTheLeadToTheMouseAfterItMovedWithBothInputs() {
        bothInputs();
        onFxThread(() -> cursor.gazeUsed());

        moveTheMouse();
        assertNull(scene.getCursor());

        onFxThread(() -> cursor.gazeUsed());
        assertNull(scene.getCursor());
    }

    @Test
    void shouldGiveTheLeadBackToTheGazeWithBothInputs() {
        bothInputs();
        moveTheMouse();

        mouseLead.set(0);
        onFxThread(() -> cursor.gazeUsed());

        assertEquals(Cursor.NONE, scene.getCursor());
    }

    @Test
    void shouldKeepThePointerInTheMenuWithBothInputs() {
        bothInputs();
        onFxThread(() -> cursor.gazeUsed());

        onFxThread(() -> cursor.setMenuOpen(true));
        assertNull(scene.getCursor());
        onFxThread(() -> cursor.gazeUsed());
        assertNull(scene.getCursor());

        onFxThread(() -> cursor.setMenuOpen(false));
        onFxThread(() -> cursor.gazeUsed());
        assertEquals(Cursor.NONE, scene.getCursor());
    }

    /**
     * Remark of the maintainer in Bubbles: a bubble passing under the hidden mouse must not start a fixation. Once hidden,
     * the mouse leaves what it was on and no longer plays; when it moves, it comes back on what is under it.
     */
    @Test
    void shouldKeepTheHiddenMouseOutOfTheGameWithBothInputs() {
        List<String> received = new ArrayList<>();
        onFxThread(() -> {
            Pane gamingRoot = new Pane();
            Rectangle bubble = new Rectangle(50, 50);
            Rectangle otherBubble = new Rectangle(50, 50);
            gamingRoot.getChildren().addAll(bubble, otherBubble);
            bubble.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> received.add("enter bubble"));
            bubble.addEventHandler(MouseEvent.MOUSE_EXITED, event -> received.add("exit bubble"));
            otherBubble.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> received.add("enter other"));

            scene = new Scene(new Pane(gamingRoot));
            cursor = GameCursor.forLastInput(scene, mouseLead::get);
            GameContextFactory.keepTheHiddenMouseOutOfTheGame(gamingRoot, cursor);
            cursor.start();
            mouseLead.set(0);

            Event.fireEvent(bubble, crossing(MouseEvent.MOUSE_ENTERED_TARGET));
            assertEquals(List.of("enter bubble"), received);

            // Hidden by the gaze: the mouse leaves the bubble, and what passes under it does not count
            cursor.gazeUsed();
            assertEquals(Cursor.NONE, scene.getCursor());
            assertEquals(List.of("enter bubble", "exit bubble"), received);
            Event.fireEvent(otherBubble, crossing(MouseEvent.MOUSE_ENTERED_TARGET));
            assertEquals(List.of("enter bubble", "exit bubble"), received);

            // It moves: back in the game, on what is under it
            mouseLead.set(LONG_LEAD);
            scene.getRoot().fireEvent(crossing(MouseEvent.MOUSE_MOVED));
            assertNull(scene.getCursor());
            assertEquals(List.of("enter bubble", "exit bubble", "enter bubble", "enter other"), received);
        });
    }

    private static MouseEvent crossing(EventType<MouseEvent> type) {
        return new MouseEvent(type, 5, 5, 5, 5, MouseButton.NONE, 0, false, false, false, false, false, false, false, false,
            false, false, null);
    }

    /**
     * The statistics screen, on the same scene, is used with the mouse; a replay comes back to the game screen.
     */
    @Test
    void shouldFollowTheScreenShown() {
        onFxThread(() -> {
            scene = new Scene(new Pane());
            cursor = GameCursor.forGazeOnly(scene, Duration.seconds(10));
            GameContextFactory.followTheScreenAndTheGaze(scene, gameRoot, cursor);
            assertNull(scene.getCursor());

            scene.setRoot(gameRoot);
            assertEquals(Cursor.NONE, scene.getCursor());

            scene.setRoot(new Pane());
            assertNull(scene.getCursor());

            scene.setRoot(gameRoot);
            assertEquals(Cursor.NONE, scene.getCursor());
        });
    }

    /**
     * One event per position, fired on the root of the game.
     */
    @Test
    void shouldHearTheGazeOnTheRootOfTheGame() {
        onFxThread(() -> {
            scene = new Scene(new Pane());
            cursor = GameCursor.forLastInput(scene, () -> 0);
            GameContextFactory.followTheScreenAndTheGaze(scene, gameRoot, cursor);
            scene.setRoot(gameRoot);
            assertNull(scene.getCursor());

            gameRoot.fireEvent(new GazeEvent(GazeEvent.GAZE_MOVED, 0, 5, 5));

            assertEquals(Cursor.NONE, scene.getCursor());
        });
    }

    @Test
    void shouldLeaveThePointerAloneWithTheMouseOnly() {
        onFxThread(() -> {
            Scene aScene = new Scene(new Pane());
            assertNull(GameContextFactory.gameCursor(aScene, EyeTracker.mouse_control, () -> 0));
            assertNotNull(GameContextFactory.gameCursor(aScene, EyeTracker.tobii, () -> 0));
            assertNotNull(GameContextFactory.gameCursor(aScene, EyeTracker.tobii_and_mouse, () -> 0));
            assertNull(GameContextFactory.gameCursor(null, EyeTracker.tobii, () -> 0));
        });
    }
}
