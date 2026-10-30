package net.gazeplay.ui.scenes.ingame;

import javafx.event.Event;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * While the configuration menu of the game screen is open, the game receives neither the mouse nor the keyboard
 * (request of the maintainer, October 2026): only the menu keeps working.
 */
class GameContextFactoryMenuTest {

    private final Pane gamingRoot = new Pane();
    private final Rectangle gameNode = new Rectangle(100, 100);
    private final HBox menu = new HBox();
    private final Rectangle menuSlider = new Rectangle(10, 10);

    GameContextFactoryMenuTest() {
        gamingRoot.getChildren().add(gameNode);
        menu.getChildren().add(menuSlider);
    }

    private static MouseEvent mouseMovedOn(Rectangle target) {
        MouseEvent event = new MouseEvent(MouseEvent.MOUSE_MOVED, 5, 5, 5, 5, MouseButton.NONE, 0,
            false, false, false, false, false, false, false, false, false, false, null);
        return event.copyFor(target, target);
    }

    private static KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, code.getChar(), code.getChar(), code, false, false, false, false);
    }

    @Test
    void shouldStopTheMouseOutsideOfTheMenuWhileItIsOpen() {
        assertTrue(GameContextFactory.blocksGameInput(mouseMovedOn(gameNode), true, menu));
        assertFalse(GameContextFactory.blocksGameInput(mouseMovedOn(menuSlider), true, menu));
    }

    @Test
    void shouldStopTheWholeKeyboardWhileTheMenuIsOpen() {
        assertTrue(GameContextFactory.blocksGameInput(keyPressed(KeyCode.SPACE), true, menu));
        assertTrue(GameContextFactory.blocksGameInput(keyPressed(KeyCode.UP), true, menu));
        // Q was the default key leaving the game (QUIT_KEY setting, removed on 30/10/2026)
        assertTrue(GameContextFactory.blocksGameInput(keyPressed(KeyCode.Q), true, menu));
    }

    @Test
    void shouldLetEverythingThroughWhenTheMenuIsClosed() {
        assertFalse(GameContextFactory.blocksGameInput(mouseMovedOn(gameNode), false, menu));
        assertFalse(GameContextFactory.blocksGameInput(keyPressed(KeyCode.SPACE), false, menu));
        assertFalse(GameContextFactory.blocksGameInput(new Event(Event.ANY), true, menu));
    }
}
