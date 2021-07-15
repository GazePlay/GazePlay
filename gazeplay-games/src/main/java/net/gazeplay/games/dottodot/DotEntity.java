package net.gazeplay.games.dottodot;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import javafx.scene.image.ImageView;

public class DotEntity extends Parent {
    private final ProgressIndicator progressIndicator;
    private Timeline progressTimeline;
    private final Stats stats;

    public DotEntity (final ImageView imageView, final Stats stats,
                      final ProgressIndicator progressIndicator, final IGameContext gameContext) {
        this.progressIndicator = progressIndicator;
        this.stats = stats;

        this.getChildren().addAll(imageView, progressIndicator);

        final EventHandler<Event> enterHandler = (Event event) -> {
            progressTimeline = new Timeline(
                new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(progressIndicator.progressProperty(), 1)));
            progressTimeline.setOnFinished(e -> nextDot());

            progressIndicator.setOpacity(1);
            progressTimeline.playFromStart();
        };

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterHandler);
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterHandler);

        final EventHandler<Event> exitHandler = (Event event) -> {
            progressIndicator.setOpacity(0);
            progressIndicator.setProgress(0);
            progressTimeline.stop();
        };

        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitHandler);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitHandler);
    }

    public void nextDot() {

    }
}
