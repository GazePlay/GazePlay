package net.gazeplay.games.drawonvideo;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.threads.GroupingThreadFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.draw.DrawBuilder;
import net.gazeplay.games.draw.ProgressiveColorPicker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VideoPlayerWithLiveFeedbackApp implements GameLifeCycle {

    private final static double canvasOpacityWhilePlaying = 0.1d;

    private final static double canvasOpacityWhileOutside = 0.7d;

    /**
     * configure the frequency of the canvas switch, when using 3 canvas
     */
    private final static long canvasSwitchFrequency = 500;

    private final WebView webview;

    private final Stats stats;
    private final IGameContext gameContext;
    private final ReplayablePseudoRandom randomGenerator;

    VideoPlayerWithLiveFeedbackApp(IGameContext gameContext, Stats stats, String youtubeVideoId) {
        super();
        this.stats = stats;
        this.gameContext = gameContext;

        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        String videoUrl = "http://www.youtube.com/embed/" + youtubeVideoId + "?autoplay=1";

        webview = new WebView();
        webview.getEngine().load(videoUrl);
        // 720p
        Dimension2D videoDimension = new Dimension2D(1280, 720);
        webview.setPrefSize(videoDimension.getWidth(), videoDimension.getHeight());

        DrawBuilder drawBuilder = new DrawBuilder(randomGenerator);
        drawBuilder.setDrawLineWidth(64);
        drawBuilder.setColorPicker(new ProgressiveColorPicker(randomGenerator));

        List<Canvas> canvasList = new ArrayList<>();

        EventHandler<Event> exitedEventHandler = mouseEvent -> {
            for (Canvas canvas : canvasList) {
                canvas.setOpacity(canvasOpacityWhileOutside);
            }
        };
        EventHandler<Event> enteredEventHandler = mouseEvent -> {
            for (Canvas canvas : canvasList) {
                drawBuilder.clear(canvas);
                canvas.setOpacity(canvasOpacityWhilePlaying);
                canvas.setVisible(true);
            }
        };

        int canvasCount = 12;
        for (int i = 0; i < canvasCount; i++) {
            Dimension2D canvasDimension = new Dimension2D(videoDimension.getWidth(),
                videoDimension.getHeight() - 80);
            Canvas canvas = drawBuilder.createCanvas(canvasDimension, stats);
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

        root.setOnMouseClicked(mouseEvent -> {
            // set the canvas to invisible
            // in order to allow mouse interaction with the WebView (YouTube controls)
            // the canvas will be set visible when the mouse exit then re-enter the scene
            for (Canvas canvas : canvasList) {
                canvas.setVisible(false);
            }
        });

        root.addEventFilter(MouseEvent.MOUSE_EXITED, exitedEventHandler);
        root.addEventFilter(MouseEvent.MOUSE_ENTERED, enteredEventHandler);

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

                    Platform.runLater(() -> {
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
                    });
                }
            }
        };

        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 3, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), new GroupingThreadFactory(this.getClass().getSimpleName()));
        executorService.execute(canvasSwitchingTask);
        stats.notifyNewRoundReady();
    }

    VideoPlayerWithLiveFeedbackApp(IGameContext gameContext, Stats stats, String youtubeVideoId, double gameSeed) {
        super();
        this.stats = stats;
        this.gameContext = gameContext;

        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        String videoUrl = "http://www.youtube.com/embed/" + youtubeVideoId + "?autoplay=1";

        webview = new WebView();
        webview.getEngine().load(videoUrl);
        // 720p
        Dimension2D videoDimension = new Dimension2D(1280, 720);
        webview.setPrefSize(videoDimension.getWidth(), videoDimension.getHeight());

        DrawBuilder drawBuilder = new DrawBuilder(randomGenerator);
        drawBuilder.setDrawLineWidth(64);
        drawBuilder.setColorPicker(new ProgressiveColorPicker(randomGenerator));

        List<Canvas> canvasList = new ArrayList<>();

        EventHandler<Event> exitedEventHandler = mouseEvent -> {
            for (Canvas canvas : canvasList) {
                canvas.setOpacity(canvasOpacityWhileOutside);
            }
        };
        EventHandler<Event> enteredEventHandler = mouseEvent -> {
            for (Canvas canvas : canvasList) {
                drawBuilder.clear(canvas);
                canvas.setOpacity(canvasOpacityWhilePlaying);
                canvas.setVisible(true);
            }
        };

        int canvasCount = 12;
        for (int i = 0; i < canvasCount; i++) {
            Dimension2D canvasDimension = new Dimension2D(videoDimension.getWidth(),
                videoDimension.getHeight() - 80);
            Canvas canvas = drawBuilder.createCanvas(canvasDimension, stats);
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

        root.setOnMouseClicked(mouseEvent -> {
            // set the canvas to invisible
            // in order to allow mouse interaction with the WebView (YouTube controls)
            // the canvas will be set visible when the mouse exit then re-enter the scene
            for (Canvas canvas : canvasList) {
                canvas.setVisible(false);
            }
        });

        root.addEventFilter(MouseEvent.MOUSE_EXITED, exitedEventHandler);
        root.addEventFilter(MouseEvent.MOUSE_ENTERED, enteredEventHandler);

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

                    Platform.runLater(() -> {
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
                    });
                }
            }
        };

        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 3, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), new GroupingThreadFactory(this.getClass().getSimpleName()));
        executorService.execute(canvasSwitchingTask);
    }

    @Override
    public void launch() {
        gameContext.setOffFixationLengthControl();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {
        if (webview != null) {
            webview.getEngine().load(null);
        }
    }

}
