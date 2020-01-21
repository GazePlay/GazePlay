package net.gazeplay.lastestnews;

import net.gazeplay.GazePlayFxApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

public class LatestNewsPopupIntegration {

    @BeforeEach
    void setup() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(GazePlayFxApp.class);
    }

    @Test
    void shouldOpenApp() {
        assert true;

    }

}
