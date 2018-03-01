package net.gazeplay.games.drawonvideo;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.draw.DrawBuilder;
import net.gazeplay.games.draw.ProgressiveColorPicker;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerWithLiveFeedbackApp implements GameLifeCycle {

    private final Dimension2D videoDimension = new Dimension2D(1280, 720); // 720p

    private final Dimension2D canvasDimension = new Dimension2D(videoDimension.getWidth(),
            videoDimension.getHeight() - 80);

    private final double canvasOpacityWhilePlaying = 0.1d;

    private final double canvasOpacityWhileOutside = 0.7d;

    /**
     * configure the frequency of the canvas switch, when using 3 canvas
     */
    private final long canvasSwitchFrequency = 500;

    private final int canvasCount = 12;

    private final GameContext gameContext;

    private final Stats stats;

    private final WebView webview;

    public VideoPlayerWithLiveFeedbackApp(GameContext gameContext, Stats stats, String youtubeVideoId) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;

        String videoUrl = "http://www.youtube.com/embed/" + youtubeVideoId + "?autoplay=1";

        webview = new WebView();
        webview.getEngine().load(videoUrl);
        webview.setPrefSize(videoDimension.getWidth(), videoDimension.getHeight());

        DrawBuilder drawBuilder = new DrawBuilder();
        drawBuilder.setBorderRectangleColor(Color.RED);
        drawBuilder.setDrawLineWidth(64);
        drawBuilder.setColorPicker(new ProgressiveColorPicker());

        List<Canvas> canvasList = new ArrayList<>();

        EventHandler<Event> exitedEventHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                for (Canvas canvas : canvasList) {
                    canvas.setOpacity(canvasOpacityWhileOutside);
                }
            }
        };
        EventHandler<Event> enteredEventHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                for (Canvas canvas : canvasList) {
                    drawBuilder.clear(canvas);
                    canvas.setOpacity(canvasOpacityWhilePlaying);
                    canvas.setVisible(true);
                }
            }
        };

        for (int i = 0; i < canvasCount; i++) {
            Canvas canvas = drawBuilder.createCanvas(canvasDimension);
            canvas.widthProperty().bind(gameContext.getRoot().widthProperty());
            canvas.heightProperty().bind(gameContext.getRoot().heightProperty());
            canvasList.add(canvas);
        }

        StackPane root = new StackPane();
        root.getChildren().add(webview);
        root.getChildren().addAll(canvasList);
        root.prefWidthProperty().bind(gameContext.getRoot().widthProperty());
        root.prefHeightProperty().bind(gameContext.getRoot().heightProperty());

        gameContext.getChildren().add(root);

        for (Canvas canvas : canvasList) {
            canvas.setOpacity(canvasOpacityWhilePlaying);
        }

        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                // set the canvas to invisible
                // in order to allow mouse interaction with the WebView (YouTube controls)
                // the canvas will be set visible when the mouse exit then re-enter the scene
                for (Canvas canvas : canvasList) {
                    canvas.setVisible(false);
                }
            }
        });

        root.addEventFilter(MouseEvent.MOUSE_EXITED, exitedEventHandler);
        root.addEventFilter(MouseEvent.MOUSE_ENTERED, enteredEventHandler);

        // root.addEventFilter(GazeEvent.GAZE_EXITED, exitedEventHandler);
        // root.addEventFilter(GazeEvent.GAZE_ENTERED, enteredEventHandler);
        // gameContext.getGazeDeviceManager().addEventFilter(root);

        Runnable canvasSwitchingTask = new Runnable() {

            int activeCanvasIndex = 0;

            int nextCanvasIndex = activeCanvasIndex + 1;

            Canvas nextCanvas = null;

            Canvas activeCanvas = null;

            Canvas previousCanvas = null;

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(canvasSwitchFrequency);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    activeCanvasIndex++;
                    if (activeCanvasIndex >= canvasList.size()) {
                        activeCanvasIndex = 0;
                    }
                    nextCanvasIndex = activeCanvasIndex + 1;
                    if (nextCanvasIndex >= canvasList.size()) {
                        nextCanvasIndex = 0;
                    }

                    previousCanvas = activeCanvas;
                    activeCanvas = canvasList.get(activeCanvasIndex);
                    nextCanvas = canvasList.get(nextCanvasIndex);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            activeCanvas.toFront();
                            if (previousCanvas != null) {
                                gameContext.getGazeDeviceManager().removeEventFilter(previousCanvas);
                            }
                            gameContext.getGazeDeviceManager().addEventFilter(activeCanvas);
                            if (nextCanvas != null) {
                                if (nextCanvas != activeCanvas) {
                                    drawBuilder.clear(nextCanvas);
                                }
                            }
                        }
                    });
                }
            }
        };
        Thread canvasSwitchingThread = new Thread(canvasSwitchingTask);

        canvasSwitchingThread.start();
    }

    @Override
    public void launch() {

    }

    @Override
    public void dispose() {
        if (webview != null) {
            webview.getEngine().load(null);
        }
    }
}
