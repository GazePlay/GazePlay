package net.gazeplay;

import javafx.event.EventTarget;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

public class TestingUtils {

    public static MouseEvent clickOnTarget(EventTarget target) {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY,
            1, false, false, false, false, false,
            false, false, false, false, false,
            new PickResult(target, 0, 0));
    }
}
