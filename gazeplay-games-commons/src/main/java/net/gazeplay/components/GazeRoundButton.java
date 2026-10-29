package net.gazeplay.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

/**
 * Round button of the games in the look of the menus (REFONTE_GRAPHIQUE.md): an icon of data/icons on a disc of the
 * surface color. A fixation on it (the fixation length of the settings) runs its action, a ring of the main color going
 * round it meanwhile; a click runs it at once. After a fixation, it must be left and looked at again to run once more.
 * Its top left corner is at (0, 0): place it with {@code setLayoutX}, {@code setLayoutY}.
 */
public class GazeRoundButton extends Group {

    private final IGameContext gameContext;

    private final Circle disc;

    private final Arc progressArc;

    private final Group icon;

    @Getter
    private final double size;

    private final Runnable action;

    private Timeline fixationTimeline;

    /**
     * Whether a fixation can run the action: false after it ran, until the gaze leaves the button.
     */
    private boolean armed = true;

    /**
     * @param size     diameter of the button
     * @param iconName name of an icon of data/icons (Material Symbols, without ".svg")
     * @param tooltip  text shown when the mouse stays on the button; none if null
     * @param action   what the button does
     */
    public GazeRoundButton(final IGameContext gameContext, final double size, final String iconName, final String tooltip,
                           final Runnable action) {
        this.gameContext = gameContext;
        this.size = size;
        this.action = action;

        disc = new Circle(size / 2, size / 2, size / 2);
        disc.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");

        final double iconSize = size * 0.55;
        final SVGPath shape = GameStyle.icon(iconName, iconSize);
        shape.setStyle("-fx-fill: -gp-accent;");
        // In a group, so that the pulse is centered on the drawn icon
        icon = new Group(shape);
        icon.setLayoutX((size - iconSize) / 2);
        icon.setLayoutY((size - iconSize) / 2);
        icon.setMouseTransparent(true);

        final double ringWidth = Math.max(4, size * 0.07);
        final double ringRadius = size / 2 - ringWidth / 2;
        progressArc = new Arc(size / 2, size / 2, ringRadius, ringRadius, 90, 0);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(null);
        progressArc.setStrokeWidth(ringWidth);
        progressArc.setStrokeLineCap(StrokeLineCap.BUTT);
        progressArc.setStyle("-fx-stroke: -gp-accent;");
        progressArc.setMouseTransparent(true);

        getChildren().addAll(disc, icon, progressArc);
        if (tooltip != null) {
            Tooltip.install(disc, new Tooltip(tooltip));
        }

        gameContext.getGazeDeviceManager().addEventFilter(disc);
        addEventFilter(MouseEvent.ANY, this::handle);
        addEventFilter(GazeEvent.ANY, this::handle);
    }

    private void handle(final Event e) {
        if (isDisabled()) {
            return;
        }
        if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
            stopFixation();
            run();
        } else if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
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
            run();
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
     * Runs the action, with a short pulse of the icon.
     */
    private void run() {
        final ScaleTransition pulse = new ScaleTransition(new Duration(180), icon);
        pulse.setToX(1.25);
        pulse.setToY(1.25);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
        action.run();
    }

    /**
     * Shows the button as active (a ring of the main color, e.g. a tool that is on) or not.
     */
    public void setActive(final boolean active) {
        disc.setStyle(active
            ? "-fx-fill: -gp-surface; -fx-stroke: -gp-accent; -fx-stroke-width: " + Math.max(3, size * 0.06) + ";"
            : "-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
    }

    /**
     * Enables or disables the button (dimmed when disabled).
     */
    public void setEnabled(final boolean enabled) {
        setDisable(!enabled);
        setOpacity(enabled ? 1 : 0.4);
        if (!enabled) {
            stopFixation();
        }
    }

    /**
     * Stops the button following the gaze, at the end of the game.
     */
    public void dispose() {
        stopFixation();
        gameContext.getGazeDeviceManager().removeEventFilter(disc);
    }
}
