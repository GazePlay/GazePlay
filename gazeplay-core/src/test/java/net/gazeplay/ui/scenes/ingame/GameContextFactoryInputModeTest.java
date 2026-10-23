package net.gazeplay.ui.scenes.ingame;

import javafx.event.Event;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
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

    @Test
    void shouldLetTheMouseThroughOtherwise() {
        assertEquals(List.of("MOUSE_MOVED", "MOUSE_PRESSED"), mouseEventsReceivedWith(EyeTracker.tobii_and_mouse));
        assertEquals(List.of("MOUSE_MOVED", "MOUSE_PRESSED"), mouseEventsReceivedWith(EyeTracker.mouse_control));
    }
}
