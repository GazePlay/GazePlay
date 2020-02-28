package net.gazeplay.commons.gaze;

import javafx.application.Platform;
import net.gazeplay.TestingUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class SecondScreenFactoryTest {

    @Test
    void shouldLaunchSecondScreen() throws InterruptedException {
        Platform.runLater(() -> {
            SecondScreen result = SecondScreenFactory.launch();
            assertNotNull(result);
        });
        TestingUtils.waitForRunLater();
    }
}
