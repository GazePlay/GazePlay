package net.gazeplay.games.whereisit;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.components.GameStyle;

/**
 * Round button with a loudspeaker, at the right of the band of the question: a fixation on it says the question
 * again. A ring of the main color goes round it during the fixation; it must be left and looked at again to say the
 * question once more.
 */
class ReplayQuestionButton extends Group {

    private final IGameContext gameContext;
    private final Circle disc;
    private final Arc progressArc;
    private final Group speaker;
    private final Runnable onActivated;

    private Timeline fixationTimeline;

    /**
     * Whether a fixation can say the question: false after it has been said, until the gaze leaves the button.
     */
    private boolean armed = true;

    /**
     * @param centerX     horizontal center of the button
     * @param centerY     vertical center of the button
     * @param size        diameter of the button
     * @param onActivated what a fixation on the button does (saying the question again)
     */
    ReplayQuestionButton(final IGameContext gameContext, final double centerX, final double centerY, final double size,
                         final Runnable onActivated) {
        this.gameContext = gameContext;
        this.onActivated = onActivated;

        disc = new Circle(centerX, centerY, size / 2);
        disc.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");

        final double iconSize = size * 0.55;
        final SVGPath icon = GameStyle.icon("volume_up_fill1", iconSize);
        icon.setStyle("-fx-fill: -gp-accent;");
        // In a group, so that the pulse is centered on the drawn icon
        speaker = new Group(icon);
        speaker.setLayoutX(centerX - iconSize / 2);
        speaker.setLayoutY(centerY - iconSize / 2);
        speaker.setMouseTransparent(true);

        final double ringWidth = Math.max(4, size * 0.07);
        final double ringRadius = size / 2 - ringWidth / 2;
        progressArc = new Arc(centerX, centerY, ringRadius, ringRadius, 90, 0);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(null);
        progressArc.setStrokeWidth(ringWidth);
        progressArc.setStrokeLineCap(StrokeLineCap.BUTT);
        progressArc.setStyle("-fx-stroke: -gp-accent;");
        progressArc.setMouseTransparent(true);

        getChildren().addAll(disc, speaker, progressArc);

        gameContext.getGazeDeviceManager().addEventFilter(disc);
        final EventHandler<Event> handler = this::handle;
        addEventFilter(MouseEvent.ANY, handler);
        addEventFilter(GazeEvent.ANY, handler);
    }

    private void handle(final Event e) {
        if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
            if (armed) {
                startFixation();
            }
        } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
            stopFixation();
            armed = true;
        }
    }

    private void startFixation() {
        stopFixation();
        fixationTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressArc.lengthProperty(), 0)),
            new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),
                new KeyValue(progressArc.lengthProperty(), -360)));
        fixationTimeline.setOnFinished(actionEvent -> {
            armed = false;
            progressArc.setLength(0);
            final ScaleTransition pulse = new ScaleTransition(new Duration(180), speaker);
            pulse.setToX(1.25);
            pulse.setToY(1.25);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            pulse.play();
            onActivated.run();
        });
        fixationTimeline.playFromStart();
    }

    private void stopFixation() {
        if (fixationTimeline != null) {
            fixationTimeline.stop();
        }
        progressArc.setLength(0);
    }

    /**
     * Stops the button following the gaze, at the end of the round.
     */
    void dispose() {
        stopFixation();
        gameContext.getGazeDeviceManager().removeEventFilter(disc);
    }
}
