package net.gazeplay.components;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class GazeFollowerIndicator extends GazeIndicator {

    private static final double GAZE_PROGRESS_INDICATOR_SIZE = 100;
    private Polygon triangle;

    public GazeFollowerIndicator(IGameContext gameContext, final Pane root, final Node mainNode) {

        super(gameContext);

        this.setOpacity(0.7);
        this.setMouseTransparent(true);

        this.setMinWidth(GAZE_PROGRESS_INDICATOR_SIZE);
        this.setMinHeight(GAZE_PROGRESS_INDICATOR_SIZE);

        triangle = new Polygon();
        triangle.getPoints().addAll(0.0, 0.0, 10.0, 20.0, 20.0, 10.0);

        triangle.visibleProperty().bind(this.visibleProperty());

        root.getChildren().add(triangle);

        root.addEventFilter(MouseEvent.MOUSE_MOVED, (event) -> {
            moveGazeIndicator(event.getX(), event.getY());
        });

        root.addEventFilter(GazeEvent.GAZE_MOVED, (event) -> {
            Point2D position = mainNode.localToScene(event.getX(), event.getY());
            moveGazeIndicator(position.getX(), position.getY());
        });
    }

    private void moveGazeIndicator(double x, double y) {
        this.toFront();
        triangle.toFront();
        triangle.setTranslateX(x);
        triangle.setTranslateY(y);
        this.setTranslateX(x);
        this.setTranslateY(y);
    }
}
