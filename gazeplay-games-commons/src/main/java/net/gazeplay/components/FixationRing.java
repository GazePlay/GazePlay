package net.gazeplay.components;

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
 * Ring of a fixation, shared by the reviewed games (first made for "Coloring", 29/10/2026; shared on 3/11/2026 for
 * "Tower Defense"): it fills in the main color during the fixation, then the action runs. In "Coloring" it follows the
 * gaze (or the mouse) on the drawing, and the zone is filled at the end. It replaces the progress indicator of the
 * former versions (a yellow disc filling up).
 */
public class FixationRing extends Group {

    private final Circle background;

    private final Arc progressArc;

    private Timeline timeline;

    private final PauseTransition hideDelay = new PauseTransition(Duration.millis(400));

    @Getter
    private boolean running;

    public FixationRing() {
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
    public void setSize(final double size) {
        final double ringWidth = Math.max(5, size * 0.12);
        background.setRadius(size / 2);
        progressArc.setRadiusX(size / 2 - ringWidth / 2);
        progressArc.setRadiusY(size / 2 - ringWidth / 2);
        progressArc.setStrokeWidth(ringWidth);
    }

    /**
     * Starts a fixation at a point: the ring fills during the given length, then runs the action.
     */
    public void start(final double x, final double y, final double lengthMs, final Runnable onFinished) {
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
     * Shows the empty ring at a point, for a game counting the fixation itself (see {@link #setProgress(double)}), as
     * "Ninja", where a short look away only stops it for a moment.
     */
    public void show(final double x, final double y) {
        stop();
        moveTo(x, y);
        setVisible(true);
        toFront();
    }

    /**
     * Fills the ring shown by {@link #show(double, double)}, from 0 to 1.
     */
    public void setProgress(final double progress) {
        progressArc.setLength(-360 * Math.max(0, Math.min(1, progress)));
    }

    /**
     * @return how much the ring is filled, from 0 to 1
     */
    public double getProgress() {
        return -progressArc.getLength() / 360;
    }

    /**
     * Moves the ring to a point.
     */
    public void moveTo(final double x, final double y) {
        setLayoutX(x);
        setLayoutY(y);
    }

    /**
     * Stops the fixation and hides the ring.
     */
    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
        hideDelay.stop();
        running = false;
        progressArc.setLength(0);
        setVisible(false);
    }
}
