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

    public GazeFollowerIndicator(IGameContext gameContext, final Pane root) {

        super(gameContext);

        this.setOpacity(0.7);
        this.setMouseTransparent(true);

        this.setMinWidth(GAZE_PROGRESS_INDICATOR_WIDTH);
        this.setMinHeight(GAZE_PROGRESS_INDICATOR_HEIGHT);

        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(0.0, 0.0, 10.0, 20.0, 20.0, 10.0);

        root.addEventFilter(MouseEvent.ANY, (event) -> {
            triangle.toFront();
            triangle.setTranslateX(event.getX());
            triangle.setTranslateY(event.getY());
        });

        root.getChildren().add(triangle);

        root.addEventFilter(MouseEvent.ANY, (event) -> {

            this.toFront();
            moveGazeIndicator(event.getX(), event.getY());
        });
        root.addEventFilter(GazeEvent.ANY, (event) -> {


            this.toFront();

            double x = event.getX();
            double y = event.getY();

            Point2D eventCoord = new Point2D(x, y);
            Point2D localCoord = root.screenToLocal(eventCoord);

            if (localCoord != null) {
                x = localCoord.getX();
                y = localCoord.getY();
            }

            moveGazeIndicator(x, y);
        });
    }

    private void moveGazeIndicator(double x, double y) {
        this.setTranslateX(x);
        this.setTranslateY(y);

    }
}
