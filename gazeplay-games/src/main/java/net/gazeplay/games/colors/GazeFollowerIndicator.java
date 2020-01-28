package net.gazeplay.games.colors;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class GazeFollowerIndicator extends AbstractGazeIndicator {

    public static final double GAZE_PROGRESS_INDICATOR_WIDTH = 100;
    public static final double GAZE_PROGRESS_INDICATOR_HEIGHT = GAZE_PROGRESS_INDICATOR_WIDTH;
    public static final double GAZE_PROGRESS_INDICATOR_OFFSET = GAZE_PROGRESS_INDICATOR_HEIGHT / 4;
    private final Polygon triangle;

    public GazeFollowerIndicator(IGameContext gameContext, final Pane root) {

        super(gameContext);

        this.setOpacity(0.7);
        this.setMouseTransparent(true);

        this.setMinWidth(GAZE_PROGRESS_INDICATOR_WIDTH);
        this.setMinHeight(GAZE_PROGRESS_INDICATOR_HEIGHT);

        triangle = new Polygon();
        triangle.getPoints().addAll(0.0, 0.0, 10.0, 20.0, 20.0, 10.0);

        root.addEventFilter(MouseEvent.MOUSE_MOVED, (event) -> {
            moveGazeIndicator(event.getX(), event.getY());
        });

        root.getChildren().add(triangle);

        root.addEventFilter(GazeEvent.GAZE_MOVED, (event) -> {
            moveGazeIndicator(event.getX(), event.getY());
        });
    }

    private void moveGazeIndicator(double x, double y) {
        this.setTranslateX(x);
        this.setTranslateY(y);

        triangle.setTranslateX(x);
        triangle.setTranslateY(y);

        triangle.toFront();
        this.toFront();
    }
}
