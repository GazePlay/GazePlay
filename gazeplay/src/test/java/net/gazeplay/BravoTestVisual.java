package net.gazeplay;

import com.sun.glass.ui.Screen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.Bravo;
import org.junit.Test;
import org.testfx.api.FxToolkit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.TestCase.assertTrue;

@Slf4j
public class BravoTestVisual {

    @Test
    public void shouldRunBravo() throws InterruptedException, TimeoutException {
        FxToolkit.registerPrimaryStage();
        BravoTestApp bravoTestApp = (BravoTestApp) FxToolkit.setupApplication(BravoTestApp.class);

        bravoTestApp.finishedAnimation.await();

        assertTrue(bravoTestApp.duration.get() < 14000);
    }

    public static class BravoTestApp extends Application {

        private final CountDownLatch finishedAnimation = new CountDownLatch(1);

        private final AtomicLong duration = new AtomicLong(0);

        @Override
        public void start(Stage primaryStage) {

            Scene scene;
            Pane root;

            primaryStage.setTitle(getClass().getSimpleName());

            primaryStage.setFullScreen(true);

            root = new BorderPane();

            Screen screen = Screen.getMainScreen();
            scene = new Scene(root, screen.getWidth(), screen.getHeight(), Color.BLACK);

            primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

            primaryStage.setScene(scene);

            Bravo bravo = new Bravo();

            root.getChildren().add(bravo);

            primaryStage.show();

            long startTime = System.currentTimeMillis();

            bravo.playWinTransition(root, 2000, actionEvent -> {
                long finishedTime = System.currentTimeMillis();

                duration.set(finishedTime - startTime);
                log.info("duration = {}", duration);

                primaryStage.close();
                finishedAnimation.countDown();

            });
        }
    }


}
