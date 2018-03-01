package net.gazeplay.commons.utils;

import com.google.common.util.concurrent.AtomicDouble;
import com.sun.glass.ui.Screen;
import javafx.animation.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertTrue;

@Slf4j
public class ScrollingBackgroundTestVisual {

    private static CountDownLatch finishedAnimation;

    private static long duration;

    @Before
    public void before() {
        finishedAnimation = new CountDownLatch(1);
    }

    @Test
    public void shouldRunBravo() throws InterruptedException {
        Application.launch(ScrollingBackgroundTestVisualApp.class, null);
        finishedAnimation.await();

        assertTrue(duration > 13000);
        assertTrue(duration < 14000);
    }

    public static class ScrollingBackgroundTestVisualApp extends Application {

        private Scene scene;
        private Group root;

        public static void createScrollingBackgroundComponent(Group componentRoot, Scene scene, Screen screen, Image image) {


            final double imageWidth = image.getWidth();
            final double imageHeight = image.getHeight();


            Rectangle backgroundRectangle = new Rectangle(0, 0, scene.getWidth(), scene.getHeight());
            componentRoot.getChildren().add(backgroundRectangle);

            final int screenWidth = Screen.getMainScreen().getWidth();
            final int screenHeight = Screen.getMainScreen().getHeight();

            final int canvasCount = (int) (screenWidth / imageWidth) + 2;
            final List<Canvas> canvasList = new ArrayList<>();

            //final Duration animationDuration = Duration.millis(screenWidth);
            //final Duration animationDuration = Duration.millis(1000);
            final Duration animationDuration = Duration.millis(imageWidth * 4);
            final int cycleCount = 4;

            final Timeline timeline = new Timeline();
            for (int i = 0; i < canvasCount; i++) {
                Canvas canvas = new Canvas(imageWidth, imageHeight);
                drawCanvas(canvas, i, image, imageWidth, screenWidth);
                canvasList.add(canvas);
                componentRoot.getChildren().add(canvas);
                final double initialTranslateX = i * imageWidth;
                canvas.setTranslateX(initialTranslateX);
                timeline.getKeyFrames().add(new KeyFrame(animationDuration, new KeyValue(canvas.translateXProperty(), initialTranslateX - imageWidth, Interpolator.LINEAR)));
            }
            timeline.setCycleCount(cycleCount);

            Canvas downCanvas = new Canvas(screenWidth, imageHeight);
            componentRoot.getChildren().add(downCanvas);


            downCanvas.setTranslateY(imageHeight);

            GraphicsContext graphicsContext = downCanvas.getGraphicsContext2D();
            for (int i = 0; i < screenWidth; i += imageWidth) {
                graphicsContext.drawImage(image, i, 0);
            }
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.setFont(new Font(22));
            graphicsContext.fillText("Canvas down", 10, 30);

            Transition animation = new Transition() {
                {

                    setCycleDuration(animationDuration);
                }

                @Override
                protected void interpolate(double frac) {
                    double visibleWidth = scene.getWidth();
                    //log.info("frac = {}", frac);
                    //graphicsContext.clearRect(0, 0, visibleWidth, downCanvas.getHeight());
                    for (int i = 0; i <= visibleWidth + imageWidth; i += imageWidth) {
                        graphicsContext.drawImage(image, i - (frac * imageWidth), 0);
                    }
                }
            };
            animation.setCycleCount(cycleCount);
            animation.setInterpolator(Interpolator.LINEAR);
            animation.setRate(1);
            animation.setAutoReverse(false);
            animation.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    timeline.play();
                }
            });


            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for (int i = 0; i < canvasCount; i++) {
                        Canvas canvas = canvasList.get(i);
                        final double initialTranslateX = i * imageWidth;
                        canvas.setTranslateX(initialTranslateX);
                    }
                    animation.play();
                }
            });


            AtomicDouble currentScale = new AtomicDouble(1);
            downCanvas.setOnScroll(event -> {
                double newScale = currentScale.get() * (1d - (event.getDeltaY() / (double) screenHeight));
                currentScale.set(newScale);
                downCanvas.setScaleX(newScale);
                downCanvas.setScaleY(newScale);
            });


            timeline.play();

            FillTransition ft = new FillTransition(Duration.millis(3000), backgroundRectangle, Color.RED, Color.BLUE);
            ft.setCycleCount(Integer.MAX_VALUE);
            ft.setAutoReverse(true);
            ft.play();

        }

        @Override
        public void start(Stage primaryStage) {

            primaryStage.setTitle(getClass().getSimpleName());

            primaryStage.setFullScreen(false);

            Screen screen = Screen.getMainScreen();

            final Image image = new Image("data/common/images/phaser-game-design-background-26.png");

            root = new Group();
            scene = new Scene(root, screen.getWidth() / 2, image.getHeight() * 2.5, Color.BLACK);

            createScrollingBackgroundComponent(root, scene, screen, image);

            primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

            primaryStage.setScene(scene);

            primaryStage.show();


        }

        private static void drawCanvas(Canvas canvas, int canvasIndex, Image image, double imageWidth, int screenWidth) {
            GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

            for (int i = 0; i < screenWidth; i += imageWidth) {
                graphicsContext.drawImage(image, i, 0);
            }

            graphicsContext.setFill(Color.BLACK);
            graphicsContext.setFont(new Font(22));
            graphicsContext.fillText("Canvas " + canvasIndex, 10, 30);
        }
    }


}
