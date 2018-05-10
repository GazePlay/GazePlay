package net.gazeplay.games.colors;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

public class GazeFollowerIndicator extends AbstractGazeIndicator {

    public static final double GAZE_PROGRESS_INDICATOR_WIDTH = 15;
    public static final double GAZE_PROGRESS_INDICATOR_HEIGHT = GAZE_PROGRESS_INDICATOR_WIDTH;
    public static final double GAZE_PROGRESS_INDICATOR_OFFSET = 2;

    public GazeFollowerIndicator(final Node root) {

        super();

        this.setMinWidth(GAZE_PROGRESS_INDICATOR_WIDTH);
        this.setMinHeight(GAZE_PROGRESS_INDICATOR_HEIGHT);

        root.addEventFilter(MouseEvent.ANY, (event) -> {

            this.toFront();
            moveGazeIndicator(event.getX() + GAZE_PROGRESS_INDICATOR_OFFSET,
                    event.getY() + GAZE_PROGRESS_INDICATOR_OFFSET);
        });
        root.addEventFilter(GazeEvent.ANY, (event) -> {

            this.toFront();
            moveGazeIndicator(event.getX() + GAZE_PROGRESS_INDICATOR_OFFSET,
                    event.getY() + GAZE_PROGRESS_INDICATOR_OFFSET);
        });
    }

    private void moveGazeIndicator(double x, double y) {
        this.setTranslateX(x);
        this.setTranslateY(y);
        /*
         * log.info("progress size : width = {}, height = {}", progressIndicator.getWidth(),
         * progressIndicator.getHeight()); log.info("translated to : x = {}, y = {}", x, y);
         */
    }
}
