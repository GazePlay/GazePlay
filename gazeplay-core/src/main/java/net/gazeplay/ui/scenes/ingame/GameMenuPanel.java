package net.gazeplay.ui.scenes.ingame;

import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * The menu of a game screen, opened and closed with the Esc key (decision of the maintainer, 5/11/2026: the gear button
 * hid the images of an evaluation in the bottom left corner, and took their mouse events). While it is closed, nothing
 * of it is on the screen: the panel is taken out of its holder, so that nothing can cover the game. It closes only on
 * request, no longer when the mouse leaves it: with the keyboard, the mouse can be anywhere when it opens.
 */
final class GameMenuPanel {

    private static final Duration FADE_LENGTH = Duration.millis(200);

    private static final String ESC_CHARACTER = "\u001B";

    private final Pane holder;

    private final Node panel;

    private final Consumer<Boolean> onOpenChange;

    private final FadeTransition fade;

    private boolean open = false;

    /**
     * Whether Esc is held down: the system repeats the key, which must not open and close the menu in turn.
     */
    private boolean keyHeld = false;

    /**
     * @param holder       where the panel goes while it is open
     * @param panel        the panel
     * @param onOpenChange called with the new state when the menu opens or closes
     */
    GameMenuPanel(final Pane holder, final Node panel, final Consumer<Boolean> onOpenChange) {
        this.holder = holder;
        this.panel = panel;
        this.onOpenChange = onOpenChange;
        panel.setVisible(false);
        panel.setDisable(true);
        panel.setMouseTransparent(true);
        panel.setOpacity(0);
        fade = new FadeTransition(FADE_LENGTH, panel);
        fade.setOnFinished(event -> {
            if (!open) {
                panel.setVisible(false);
                holder.getChildren().remove(panel);
            }
        });
    }

    boolean isOpen() {
        return open;
    }

    void toggle() {
        setOpen(!open);
    }

    void close() {
        keyHeld = false;
        setOpen(false);
    }

    /**
     * Opens or closes the menu when the event is the Esc key.
     *
     * @return whether the event is the Esc key, which is kept from the game
     */
    boolean handleKey(final Event event) {
        if (!(event instanceof KeyEvent key)) {
            return false;
        }
        if (key.getEventType() == KeyEvent.KEY_TYPED) {
            return ESC_CHARACTER.equals(key.getCharacter());
        }
        if (key.getCode() != KeyCode.ESCAPE) {
            return false;
        }
        if (key.getEventType() == KeyEvent.KEY_RELEASED) {
            keyHeld = false;
        } else if (!keyHeld) {
            keyHeld = true;
            toggle();
        }
        return true;
    }

    private void setOpen(final boolean value) {
        if (value == open) {
            return;
        }
        open = value;
        fade.stop();
        panel.setDisable(!value);
        panel.setMouseTransparent(!value);
        if (value) {
            if (!holder.getChildren().contains(panel)) {
                holder.getChildren().add(panel);
            }
            panel.setVisible(true);
        }
        fade.setFromValue(panel.getOpacity());
        fade.setToValue(value ? 1 : 0);
        fade.playFromStart();
        onOpenChange.accept(value);
    }

}
