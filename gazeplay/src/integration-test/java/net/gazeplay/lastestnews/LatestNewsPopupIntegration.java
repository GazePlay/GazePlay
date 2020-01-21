package net.gazeplay.lastestnews;

import net.gazeplay.GazePlayFxApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

public class LatestNewsPopupIntegration {

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setup() throws TimeoutException {
        GazePlayFxApp.setApplicationContext(applicationContext);

        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(GazePlayFxApp.class);
    }

    @Test
    void shouldOpenApp() {
        assert true;

    }

}
