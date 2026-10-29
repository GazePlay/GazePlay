package net.gazeplay.games.colors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.util.function.Consumer;

/**
 * A color of the tool box of the "Coloring" game: a round swatch, chosen by a fixation (a ring of the main color goes
 * round it meanwhile) or a click. The chosen color is circled in the main color. Its top left corner is at (0, 0).
 */
public class ColorBox extends Group {

    private final IGameContext gameContext;

    private final Circle swatch;

    private final Circle selectionRing;

    private final Arc progressArc;

    private final Consumer<ColorBox> onChosen;

    @Getter
    private Color color;

    @Getter
    private boolean selected;

    private Timeline fixationTimeline;

    /**
     * @param size     diameter of the swatch and its ring
     * @param onChosen what choosing this color does (e.g. making it the color of the coloring)
     */
    public ColorBox(final IGameContext gameContext, final Color color, final double size,
                    final Consumer<ColorBox> onChosen) {
        this.gameContext = gameContext;
        this.color = color;
        this.onChosen = onChosen;

        final double ringWidth = Math.max(4, size * 0.08);
        selectionRing = new Circle(size / 2, size / 2, size / 2 - ringWidth / 2);
        selectionRing.setFill(null);
        selectionRing.setStrokeWidth(ringWidth);
        selectionRing.setStyle("-fx-stroke: -gp-accent;");
        selectionRing.setVisible(false);
        selectionRing.setMouseTransparent(true);

        swatch = new Circle(size / 2, size / 2, size / 2 - ringWidth * 1.6);
        swatch.setFill(color);
        // An outline, so that white and the light colors stay visible on a light background
        swatch.setStyle("-fx-stroke: -gp-text-muted; -fx-stroke-width: 1.5;");

        progressArc = new Arc(size / 2, size / 2, size / 2 - ringWidth / 2, size / 2 - ringWidth / 2, 90, 0);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(null);
        progressArc.setStrokeWidth(ringWidth);
        progressArc.setStrokeLineCap(StrokeLineCap.BUTT);
        progressArc.setStyle("-fx-stroke: -gp-accent;");
        progressArc.setMouseTransparent(true);

        getChildren().addAll(swatch, selectionRing, progressArc);

        gameContext.getGazeDeviceManager().addEventFilter(swatch);
        addEventFilter(MouseEvent.ANY, this::handle);
        addEventFilter(GazeEvent.ANY, this::handle);
    }

    private void handle(final Event e) {
        if (selected) {
            return;
        }
        if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
            stopFixation();
            onChosen.accept(this);
        } else if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
            stopFixation();
            fixationTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressArc.lengthProperty(), 0)),
                new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(progressArc.lengthProperty(), -360)));
            fixationTimeline.setOnFinished(actionEvent -> {
                progressArc.setLength(0);
                onChosen.accept(this);
            });
            fixationTimeline.playFromStart();
        } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
            stopFixation();
        }
    }

    private void stopFixation() {
        if (fixationTimeline != null) {
            fixationTimeline.stop();
        }
        progressArc.setLength(0);
    }

    /**
     * Marks the swatch as the chosen color, or not.
     */
    public void setSelected(final boolean selected) {
        this.selected = selected;
        selectionRing.setVisible(selected);
        stopFixation();
    }

    /**
     * Changes the color of the swatch.
     */
    public void setColor(final Color color) {
        this.color = color;
        swatch.setFill(color);
    }

    /**
     * Stops the swatch following the gaze, at the end of the game.
     */
    public void dispose() {
        stopFixation();
        gameContext.getGazeDeviceManager().removeEventFilter(swatch);
    }
}
