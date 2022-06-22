package net.gazeplay.games.draw;

import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DrawBuilder {

    @Setter
    private ColorPicker colorPicker;

    @Setter
    private int drawLineWidth = 8;

    Stats stats;
    private final boolean inReplayMode;

    public DrawBuilder(ReplayablePseudoRandom randomGenerator, boolean inReplayMode) {
        colorPicker = new RandomColorPicker(randomGenerator);
        this.inReplayMode = inReplayMode;
    }

    public Canvas createCanvas(final Scene scene, final double coefficient, Stats stats) {

        Dimension2D canvasDimension = new Dimension2D(scene.getWidth() / coefficient, scene.getHeight() / coefficient);
        final Canvas canvas = createCanvas(canvasDimension, stats);

        canvas.widthProperty().bind(scene.widthProperty().divide(coefficient));
        canvas.heightProperty().bind(scene.heightProperty().divide(coefficient));

        return canvas;
    }

    public Canvas createCanvas(final Dimension2D canvasDimension, Stats stats) {
        this.stats = stats;
        final Canvas canvas = new Canvas(canvasDimension.getWidth(), canvasDimension.getHeight());
        final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        initDraw(graphicsContext);

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            graphicsContext.lineTo(event.getX(), event.getY());
            // graphicsContext.setStroke(colorPicker.pickColor());
            graphicsContext.stroke();
        });

        canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            changeColor(graphicsContext);
        });

        canvas.addEventHandler(MouseEvent.MOUSE_EXITED, event -> graphicsContext.closePath());

        canvas.addEventFilter(GazeEvent.GAZE_MOVED, new EventHandler<>() {

            final AtomicInteger rateLimiter = new AtomicInteger(0);

            private static final int RATE_LIMIT = 5;

            @Override
            public void handle(final GazeEvent event) {
                log.debug("GAZE_MOVED : event = " + event);
                final int rateLimiterValue = rateLimiter.incrementAndGet();
                if (rateLimiterValue == RATE_LIMIT) {
                    rateLimiter.set(0);

                    if (((event.getX() - canvas.getWidth() / 2) * (event.getX() - canvas.getWidth() / 2) + (event.getY() - canvas.getHeight() / 2) * (event.getY() - canvas.getHeight() / 2)) > 0.15) {
                        graphicsContext.lineTo(event.getX(), event.getY());
                        // graphicsContext.setStroke(colorPicker.pickColor());
                        graphicsContext.stroke();
                    }
                }
            }
        });

        canvas.addEventFilter(GazeEvent.GAZE_ENTERED, event -> {
            changeColor(graphicsContext);
        });

        canvas.addEventFilter(GazeEvent.GAZE_EXITED, event -> graphicsContext.closePath());

        return canvas;
    }

    private void changeColor(GraphicsContext graphicsContext) {
        if (!inReplayMode) {
            stats.incrementNumberOfGoalsReached();
        }
        graphicsContext.setStroke(colorPicker.pickColor());
        graphicsContext.beginPath();
    }

    private void initDraw(final GraphicsContext gc) {
        gc.setLineWidth(drawLineWidth);
    }

    public void clear(final Canvas canvas) {
        initDraw(canvas.getGraphicsContext2D());
    }

}
