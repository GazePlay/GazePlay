package net.gazeplay.games.spotthedifferences;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.GameStyle;

import java.util.List;

/**
 * A difference between the two pictures of SpotTheDifferences (reviewed on 30/10/2026): a circle at the same place on
 * each picture. Looking at either one for the fixation length of the settings finds it (it used to need the gaze to
 * move after 1 s); nothing shows during the fixation, which would tell where it is. Found, it is circled in green on
 * both pictures. A hint makes both circles blink.
 */
public class Difference {

    private final IGameContext gameContext;

    @Getter
    private final List<Circle> circles;

    private final List<TargetAOI> areas;

    private final Runnable onFound;

    private final EventHandler<Event> handler = this::handle;

    private PauseTransition fixation;

    private FadeTransition hint;

    @Getter
    private boolean found;

    /**
     * @param circles the circle of the difference on each picture, placed by the game
     * @param areas   the target areas of the statistics of the circles
     * @param onFound what to do when it is found
     */
    Difference(final IGameContext gameContext, final List<Circle> circles, final List<TargetAOI> areas,
               final Runnable onFound) {
        this.gameContext = gameContext;
        this.circles = circles;
        this.areas = areas;
        this.onFound = onFound;
        for (final Circle circle : circles) {
            // Transparent, but the whole disc follows the gaze
            circle.setFill(Color.TRANSPARENT);
            circle.setStroke(null);
            circle.setStrokeWidth(Math.max(4, circle.getRadius() * 0.08));
            gameContext.getGazeDeviceManager().addEventFilter(circle);
            circle.addEventFilter(MouseEvent.ANY, handler);
            circle.addEventFilter(GazeEvent.ANY, handler);
        }
    }

    private void handle(final Event e) {
        if (found) {
            return;
        }
        if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
            stopFixation();
            fixation = new PauseTransition(Duration.millis(gameContext.getConfiguration().getFixationLength()));
            fixation.setOnFinished(event -> found());
            fixation.play();
        } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
            stopFixation();
        }
    }

    private void stopFixation() {
        if (fixation != null) {
            fixation.stop();
            fixation = null;
        }
    }

    /**
     * Marks the difference found: circled in green on both pictures, then tells the game.
     */
    void found() {
        if (found) {
            return;
        }
        found = true;
        stopFixation();
        stopHint();
        final long now = System.currentTimeMillis();
        areas.forEach(area -> area.setTimeEnded(now));
        for (final Circle circle : circles) {
            circle.setStroke(Color.web(GameStyle.SUCCESS_COLOR));
            circle.setOpacity(0);
            final FadeTransition fade = new FadeTransition(Duration.millis(250), circle);
            fade.setToValue(1);
            final ScaleTransition scale = new ScaleTransition(Duration.millis(250), circle);
            scale.setFromX(1.4);
            scale.setFromY(1.4);
            scale.setToX(1);
            scale.setToY(1);
            new ParallelTransition(fade, scale).play();
        }
        onFound.run();
    }

    /**
     * Makes both circles blink in the main color, a few times.
     */
    void showHint() {
        if (found) {
            return;
        }
        for (final Circle circle : circles) {
            circle.setStyle("-fx-stroke: -gp-accent;");
            circle.setOpacity(0);
        }
        hint = new FadeTransition(Duration.millis(450));
        hint.setFromValue(0);
        hint.setToValue(1);
        hint.setAutoReverse(true);
        hint.setCycleCount(6);
        hint.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            final double opacity = circles.get(0).getOpacity();
            circles.forEach(circle -> circle.setOpacity(opacity));
        });
        hint.setNode(circles.get(0));
        hint.setOnFinished(event -> stopHint());
        hint.play();
    }

    private void stopHint() {
        if (hint != null) {
            hint.stop();
            hint = null;
        }
        for (final Circle circle : circles) {
            circle.setStyle(null);
            circle.setOpacity(1);
        }
    }

    /**
     * Ends the target areas of a difference not found (the picture is left), and stops following the gaze.
     */
    void release() {
        stopFixation();
        stopHint();
        final long now = System.currentTimeMillis();
        if (!found) {
            areas.forEach(area -> area.setTimeEnded(now));
        }
        for (final Circle circle : circles) {
            gameContext.getGazeDeviceManager().removeEventFilter(circle);
        }
    }
}
