package net.gazeplay.games.colors;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class GazeFollowerIndicator extends AbstractGazeIndicator {

    public static final double GAZE_PROGRESS_INDICATOR_WIDTH = 100;
    public static final double GAZE_PROGRESS_INDICATOR_HEIGHT = GAZE_PROGRESS_INDICATOR_WIDTH;
    public static final double GAZE_PROGRESS_INDICATOR_OFFSET = GAZE_PROGRESS_INDICATOR_HEIGHT / 4;

    public GazeFollowerIndicator(final Node root) {

        super();

        this.setOpacity(0.7);
        this.setMouseTransparent(true);

        this.setMinWidth(GAZE_PROGRESS_INDICATOR_WIDTH);
        this.setMinHeight(GAZE_PROGRESS_INDICATOR_HEIGHT);

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
        this.setTranslateX(x - (GAZE_PROGRESS_INDICATOR_WIDTH) / 2);
        this.setTranslateY(y + GAZE_PROGRESS_INDICATOR_OFFSET - GAZE_PROGRESS_INDICATOR_HEIGHT / 2);

    }
}
