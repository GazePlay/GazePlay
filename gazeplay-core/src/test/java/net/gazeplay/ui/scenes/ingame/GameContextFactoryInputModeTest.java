package net.gazeplay.ui.scenes.ingame;

import javafx.event.Event;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.EyeTracker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Which inputs reach the games (decision of the maintainer, October 2026): with "tobii" alone, the mouse used to fill
 * the progress indicators too.
 */
class GameContextFactoryInputModeTest {

    private static List<String> mouseEventsReceivedWith(EyeTracker eyeTracker) {
        Pane gamingRoot = new Pane();
        Rectangle progressIndicator = new Rectangle(100, 100);
        gamingRoot.getChildren().add(progressIndicator);
        List<String> received = new ArrayList<>();
        progressIndicator.addEventHandler(MouseEvent.ANY, e -> received.add(e.getEventType().getName()));

        GameContextFactory.applyInputMode(gamingRoot, eyeTracker);

        for (var type : List.of(MouseEvent.MOUSE_MOVED, MouseEvent.MOUSE_PRESSED)) {
            Event.fireEvent(progressIndicator, new MouseEvent(type, 10, 10, 10, 10, MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, false, null));
        }
        return received;
    }

    @Test
    void shouldStopTheMouseWhenOnlyTheEyeTrackerPlays() {
        assertEquals(List.of(), mouseEventsReceivedWith(EyeTracker.tobii));
    }

    /**
     * A screen marked for the mouse (the participant form of GazePlay Eval, filled by the examiner) gets it, with its
     * children, even with the eye tracker alone; the rest of the game area does not (maintainer's request of
     * 30/10/2026).
     */
    @Test
    void shouldLetTheMouseThroughAMarkedScreenWithTheEyeTrackerAlone() {
        Pane gamingRoot = new Pane();
        Pane form = new Pane();
        form.getProperties().put(IGameContext.MOUSE_ALLOWED_PROPERTY, true);
        Rectangle startButton = new Rectangle(10, 10);
        form.getChildren().add(startButton);
        Rectangle target = new Rectangle(10, 10);
        gamingRoot.getChildren().addAll(form, target);
        List<String> received = new ArrayList<>();
        startButton.addEventHandler(MouseEvent.ANY, e -> received.add("button " + e.getEventType().getName()));
        target.addEventHandler(MouseEvent.ANY, e -> received.add("target " + e.getEventType().getName()));

        GameContextFactory.applyInputMode(gamingRoot, EyeTracker.tobii);

        for (Rectangle node : List.of(startButton, target)) {
            Event.fireEvent(node, new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null));
        }
        assertEquals(List.of("button MOUSE_CLICKED"), received);
    }

    @Test
    void shouldLetTheMouseThroughOtherwise() {
        assertEquals(List.of("MOUSE_MOVED", "MOUSE_PRESSED"), mouseEventsReceivedWith(EyeTracker.tobii_and_mouse));
        assertEquals(List.of("MOUSE_MOVED", "MOUSE_PRESSED"), mouseEventsReceivedWith(EyeTracker.mouse_control));
    }
}
