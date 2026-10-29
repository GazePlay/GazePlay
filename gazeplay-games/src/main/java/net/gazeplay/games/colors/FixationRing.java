package net.gazeplay.games.colors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;

/**
 * Ring following the gaze (or the mouse) on the drawing of the "Coloring" game: it fills in the main color during the
 * fixation, then the zone is filled. It replaces the progress indicator of the former versions.
 */
class FixationRing extends Group {

    private final Circle background;

    private final Arc progressArc;

    private Timeline timeline;

    private final PauseTransition hideDelay = new PauseTransition(Duration.millis(400));

    @Getter
    private boolean running;

    FixationRing() {
        background = new Circle();
        background.setStyle("-fx-fill: rgba(255, 255, 255, 0.35); -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        progressArc = new Arc();
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(null);
        progressArc.setStrokeLineCap(StrokeLineCap.ROUND);
        progressArc.setStartAngle(90);
        progressArc.setStyle("-fx-stroke: -gp-accent;");
        getChildren().addAll(background, progressArc);
        setMouseTransparent(true);
        setVisible(false);
        hideDelay.setOnFinished(event -> setVisible(false));
        setSize(60);
    }

    /**
     * Sets the diameter of the ring.
     */
    void setSize(final double size) {
        final double ringWidth = Math.max(5, size * 0.12);
        background.setRadius(size / 2);
        progressArc.setRadiusX(size / 2 - ringWidth / 2);
        progressArc.setRadiusY(size / 2 - ringWidth / 2);
        progressArc.setStrokeWidth(ringWidth);
    }

    /**
     * Starts a fixation at a point: the ring fills during the given length, then runs the action.
     */
    void start(final double x, final double y, final double lengthMs, final Runnable onFinished) {
        stop();
        moveTo(x, y);
        setVisible(true);
        toFront();
        running = true;
        timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressArc.lengthProperty(), 0)),
            new KeyFrame(Duration.millis(lengthMs), new KeyValue(progressArc.lengthProperty(), -360)));
        timeline.setOnFinished(event -> {
            running = false;
            onFinished.run();
            hideDelay.playFromStart();
        });
        timeline.playFromStart();
    }

    /**
     * Moves the ring to a point.
     */
    void moveTo(final double x, final double y) {
        setLayoutX(x);
        setLayoutY(y);
    }

    /**
     * Stops the fixation and hides the ring.
     */
    void stop() {
        if (timeline != null) {
            timeline.stop();
        }
        hideDelay.stop();
        running = false;
        progressArc.setLength(0);
        setVisible(false);
    }
}
