package net.gazeplay.ui.scenes.ingame;

import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The menu of a game screen, opened and closed with Esc (decision of the maintainer, 5/11/2026): nothing of it stays on
 * the screen while it is closed, where the gear button hid the images in the corner of an evaluation.
 */
@ExtendWith(ApplicationExtension.class)
class GameMenuPanelTest {

    private final Pane holder = new Pane();
    private final HBox panel = new HBox();
    private final List<Boolean> changes = new ArrayList<>();

    private GameMenuPanel menu() {
        return new GameMenuPanel(holder, panel, changes::add);
    }

    private static KeyEvent key(javafx.event.EventType<KeyEvent> type, KeyCode code) {
        return new KeyEvent(type, code.getChar(), code.getChar(), code, false, false, false, false);
    }

    private static void onFxThread(Runnable action) {
        WaitForAsyncUtils.waitForAsyncFx(5000, action);
    }

    @Test
    void shouldLeaveNothingOnTheScreenWhileClosed() {
        menu();

        assertTrue(holder.getChildren().isEmpty());
        assertFalse(panel.isVisible());
        assertTrue(panel.isDisabled());
    }

    @Test
    void shouldOpenThenCloseWithEsc() {
        GameMenuPanel menu = menu();

        onFxThread(() -> {
            assertTrue(menu.handleKey(key(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE)));
            assertTrue(menu.handleKey(key(KeyEvent.KEY_RELEASED, KeyCode.ESCAPE)));
        });

        assertTrue(menu.isOpen());
        assertTrue(holder.getChildren().contains(panel));
        assertTrue(panel.isVisible());
        assertFalse(panel.isDisabled());

        onFxThread(() -> menu.handleKey(key(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE)));

        assertFalse(menu.isOpen());
        assertTrue(panel.isDisabled());
        assertEquals(List.of(true, false), changes);
    }

    @Test
    void shouldNotFlickerWhileEscIsHeldDown() {
        GameMenuPanel menu = menu();

        onFxThread(() -> {
            menu.handleKey(key(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
            menu.handleKey(key(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
            menu.handleKey(key(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
        });

        assertTrue(menu.isOpen());
        assertEquals(List.of(true), changes);
    }

    @Test
    void shouldTakeThePanelOutOfItsHolderOnceClosed() throws TimeoutException {
        GameMenuPanel menu = menu();

        onFxThread(menu::toggle);
        onFxThread(menu::close);

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> holder.getChildren().isEmpty());
        assertFalse(panel.isVisible());
    }

    @Test
    void shouldLeaveTheOtherKeysAlone() {
        GameMenuPanel menu = menu();

        onFxThread(() -> assertFalse(menu.handleKey(key(KeyEvent.KEY_PRESSED, KeyCode.SPACE))));

        assertFalse(menu.isOpen());
    }

    /**
     * On a real scene: the game never sees Esc, and sees nothing else while the menu is open.
     */
    @Test
    void shouldKeepEscFromTheGameAndStopTheKeyboardWhileOpen() {
        Pane root = new Pane();
        List<KeyCode> receivedByTheGame = new ArrayList<>();
        onFxThread(() -> {
            Scene scene = new Scene(root);
            scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> receivedByTheGame.add(event.getCode()));
            GameMenuPanel menu = menu();
            GameContextFactory.connectMenuToScene(scene, root, menu, holder);

            Event.fireEvent(scene, key(KeyEvent.KEY_PRESSED, KeyCode.SPACE));
            Event.fireEvent(scene, key(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
            assertTrue(menu.isOpen());
            Event.fireEvent(scene, key(KeyEvent.KEY_RELEASED, KeyCode.ESCAPE));
            Event.fireEvent(scene, key(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
            Event.fireEvent(scene, key(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
            assertFalse(menu.isOpen());
        });

        assertEquals(List.of(KeyCode.SPACE), receivedByTheGame);
    }

    /**
     * The home and replay buttons replace the screen: the mouse leaving the menu used to close it, and the next game
     * would start paused.
     */
    @Test
    void shouldCloseWhenAnotherScreenReplacesTheGame() {
        Pane root = new Pane();
        onFxThread(() -> {
            Scene scene = new Scene(root);
            GameMenuPanel menu = menu();
            GameContextFactory.connectMenuToScene(scene, root, menu, holder);
            menu.toggle();

            scene.setRoot(new Pane());

            assertFalse(menu.isOpen());
            // On the statistics screen, Esc is no longer for this menu
            Event.fireEvent(scene, key(KeyEvent.KEY_PRESSED, KeyCode.ESCAPE));
            assertFalse(menu.isOpen());
        });

        assertEquals(List.of(true, false), changes);
    }

}
