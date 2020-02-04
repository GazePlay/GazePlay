package net.gazeplay.games.draw;

import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DrawBuilder {

    @Setter
    private Color borderRectangleColor = Color.WHITE;

    @Setter
    private ColorPicker colorPicker = new RandomColorPicker();

    @Setter
    private int drawLineWidth = 8;

    public Canvas createCanvas(final Dimension2D canvasDimension, final double coefficient) {
        final Canvas canvas = createCanvas(canvasDimension);
        canvas.setLayoutX(canvasDimension.getWidth() * (coefficient - 1) / 2);
        canvas.setLayoutY(canvasDimension.getHeight() * (coefficient - 1) / 2);
        return canvas;
    }

    public Canvas createCanvas(final Dimension2D canvasDimension) {
        final Canvas canvas = new Canvas(canvasDimension.getWidth(), canvasDimension.getHeight());

        final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        initDraw(graphicsContext);

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            graphicsContext.lineTo(event.getX(), event.getY());
            // graphicsContext.setStroke(colorPicker.pickColor());
            graphicsContext.stroke();
        });

        canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            graphicsContext.setStroke(colorPicker.pickColor());
            graphicsContext.beginPath();
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

                    graphicsContext.lineTo(event.getX() - canvas.getLayoutX(), event.getY() - canvas.getLayoutY());
                    // graphicsContext.setStroke(colorPicker.pickColor());
                    graphicsContext.stroke();
                }
            }
        });

        canvas.addEventFilter(GazeEvent.GAZE_ENTERED, event -> {
            graphicsContext.setStroke(colorPicker.pickColor());
            graphicsContext.beginPath();
        });

        canvas.addEventFilter(GazeEvent.GAZE_EXITED, event -> graphicsContext.closePath());

        return canvas;
    }

    private void initDraw(final GraphicsContext gc) {
        final double canvasWidth = gc.getCanvas().getWidth();
        final double canvasHeight = gc.getCanvas().getHeight();

        gc.setFill(Color.WHITE);
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.setStroke(borderRectangleColor);
        gc.setLineWidth(5);
        gc.strokeRect(0, // x of the upper left corner
            0, // y of the upper left corner
            canvasWidth, // width of the rectangle
            canvasHeight); // height of the rectangle

        gc.setFill(Color.RED);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(drawLineWidth);
    }

    public void clear(final Canvas canvas) {
        initDraw(canvas.getGraphicsContext2D());
    }

}
