package net.gazeplay;

import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

import java.util.concurrent.Semaphore;

public class TestingUtils {

    public static MouseEvent clickOnTarget(EventTarget target) {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY,
            1, false, false, false, false, false,
            false, false, false, false, false,
            new PickResult(target, 0, 0));
    }

    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);
        semaphore.acquire();
    }
}
