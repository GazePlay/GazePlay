package net.gazeplay.commons.utils;

import com.sun.glass.ui.Screen;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GamePanelDimensionProvider;
import net.gazeplay.ui.scenes.ingame.GameContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class BravoTest {

    @Mock
    private GameContext mockGameContext;

    @Mock
    private GamePanelDimensionProvider mockDimensionProvider;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldPlayWinTransition() throws InterruptedException, TimeoutException {
        FxToolkit.registerPrimaryStage();
        BravoTestApp bravoTestApp = (BravoTestApp) FxToolkit.setupApplication(BravoTestApp.class);

        bravoTestApp.testWinAnimation();
        bravoTestApp.finishedAnimation.await();

        FxToolkit.cleanupStages();
        assertTrue(bravoTestApp.duration.get() < 10000);
    }

    @Test
    public void shouldPlayWinTransitionNoDelay() throws InterruptedException, TimeoutException {
        FxToolkit.registerPrimaryStage();
        BravoTestApp bravoTestApp = (BravoTestApp) FxToolkit.setupApplication(BravoTestApp.class);

        bravoTestApp.testWinAnimationNoDelay();
        bravoTestApp.finishedAnimation.await();

        FxToolkit.cleanupStages();
        assertTrue(bravoTestApp.duration.get() < 14000);
    }

    @Test
    public void shouldSetConfettiOnStart() throws TimeoutException {
        ObservableList<Node> mockList = FXCollections.observableArrayList();

        when(mockGameContext.getGamePanelDimensionProvider()).thenReturn(mockDimensionProvider);
        when(mockGameContext.getChildren()).thenReturn(mockList);
        when(mockDimensionProvider.getDimension2D()).thenReturn(new Dimension2D(100d, 100d));

        FxToolkit.registerPrimaryStage();
        BravoTestApp bravoTestApp = (BravoTestApp) FxToolkit.setupApplication(BravoTestApp.class);

        Bravo bravo = bravoTestApp.bravo;

        bravo.setConfettiOnStart(mockGameContext);

        FxToolkit.cleanupStages();
        assertEquals(101, mockList.size());
    }

    public static class BravoTestApp extends Application {

        private final CountDownLatch finishedAnimation = new CountDownLatch(1);

        private final AtomicLong duration = new AtomicLong(0);

        private Bravo bravo;

        private Pane root;

        @Override
        public void start(Stage primaryStage) {
            Scene scene;

            primaryStage.setTitle(getClass().getSimpleName());
            primaryStage.setFullScreen(true);

            root = new BorderPane();

            Screen screen = Screen.getMainScreen();
            scene = new Scene(root, screen.getWidth(), screen.getHeight(), Color.BLACK);

            primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
            primaryStage.setScene(scene);

            bravo = new Bravo();
            root.getChildren().add(bravo);

            primaryStage.show();
        }

        public void testWinAnimation() {
            long startTime = System.currentTimeMillis();

            bravo.playWinTransition(root, 2000, actionEvent -> {
                long finishedTime = System.currentTimeMillis();

                duration.set(finishedTime - startTime);
                log.info("duration = {}", duration);

                finishedAnimation.countDown();
            });
        }

        public void testWinAnimationNoDelay() {
            long startTime = System.currentTimeMillis();

            bravo.playWinTransition(root, actionEvent -> {
                long finishedTime = System.currentTimeMillis();

                duration.set(finishedTime - startTime);
                log.info("duration = {}", duration);

                finishedAnimation.countDown();
            });
        }
    }
}
