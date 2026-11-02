package net.gazeplay.games.flowerOfNumbers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.components.GameStyle;

/**
 * A value of Flower Of Numbers to look at (reviewed on 2/11/2026): its picture or text on a rounded card; a fixation
 * (the fixation length of the settings) runs its action, a ring of the main color going round the card meanwhile.
 * After that, it must be left and looked at again to run once more. Its top left corner is at (0, 0).
 */
class ValueTarget extends Group {

    private final IGameContext gameContext;

    private final Rectangle background;

    private final Rectangle ring;

    private final double ringPerimeter;

    private final DoubleProperty progress = new SimpleDoubleProperty(0);

    /**
     * What a fixation does (set after the creation when it needs the target itself).
     */
    @Setter
    private Runnable action;

    @Getter
    private final int value;

    @Getter
    private final double width;

    @Getter
    private final double height;

    private Timeline fixation;

    private boolean armed = true;

    @Getter
    private boolean enabled = true;

    /**
     * @param content the picture or the text, its top left corner at (0, 0), centered on the card
     * @param fill    the color of the card (CSS)
     */
    ValueTarget(final IGameContext gameContext, final int value, final Node content, final double width,
                final double height, final String fill, final Runnable action) {
        this.gameContext = gameContext;
        this.value = value;
        this.width = width;
        this.height = height;
        this.action = action;

        final double radius = GameStyle.cardRadius(width, height);
        background = new Rectangle(width, height);
        background.setArcWidth(2 * radius);
        background.setArcHeight(2 * radius);
        background.setStrokeType(StrokeType.INSIDE);
        background.setStyle("-fx-fill: " + fill + "; -fx-stroke: -gp-border; -fx-stroke-width: 2;");

        content.setLayoutX((width - content.getLayoutBounds().getWidth()) / 2 - content.getLayoutBounds().getMinX());
        content.setLayoutY((height - content.getLayoutBounds().getHeight()) / 2 - content.getLayoutBounds().getMinY());
        content.setMouseTransparent(true);

        ring = new Rectangle(width, height);
        ring.setArcWidth(2 * radius);
        ring.setArcHeight(2 * radius);
        ring.setFill(null);
        ring.setStrokeType(StrokeType.INSIDE);
        ring.setStrokeLineCap(StrokeLineCap.BUTT);
        ring.setStrokeWidth(Math.max(4, Math.min(width, height) * 0.08));
        ring.setStyle("-fx-stroke: -gp-accent;");
        ring.setMouseTransparent(true);
        ring.setVisible(false);
        ringPerimeter = 2 * (width + height) - (8 - 2 * Math.PI) * radius;
        progress.addListener((observable, oldValue, newValue) ->
            ring.getStrokeDashArray().setAll(Math.max(0.01, newValue.doubleValue() * ringPerimeter), ringPerimeter));

        getChildren().addAll(background, content, ring);

        gameContext.getGazeDeviceManager().addEventFilter(background);
        addEventFilter(MouseEvent.ANY, this::handle);
        addEventFilter(GazeEvent.ANY, this::handle);
    }

    private void handle(final Event e) {
        if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
            if (enabled && armed) {
                startFixation();
            }
        } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
            stopFixation();
            armed = true;
        }
    }

    private void startFixation() {
        stopFixation();
        ring.setVisible(true);
        fixation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(progress, 0)),
            new KeyFrame(Duration.millis(gameContext.getConfiguration().getFixationLength()), new KeyValue(progress, 1)));
        fixation.setOnFinished(event -> {
            armed = false;
            stopFixation();
            if (action != null) {
                action.run();
            }
        });
        fixation.playFromStart();
    }

    private void stopFixation() {
        if (fixation != null) {
            fixation.stop();
            fixation = null;
        }
        progress.set(0);
        ring.setVisible(false);
    }

    /**
     * Runs the action as at the end of a fixation (for the tests).
     */
    void select() {
        if (enabled && action != null) {
            armed = false;
            stopFixation();
            action.run();
        }
    }

    /**
     * Enables or disables the value (dimmed when disabled).
     */
    void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        setOpacity(enabled ? 1 : 0.35);
        if (!enabled) {
            stopFixation();
        }
    }

    /**
     * Stops following the gaze.
     */
    void dispose() {
        stopFixation();
        gameContext.getGazeDeviceManager().removeEventFilter(background);
    }
}
