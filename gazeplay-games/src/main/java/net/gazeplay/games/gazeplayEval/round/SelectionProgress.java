package net.gazeplay.games.gazeplayEval.round;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;
import net.gazeplay.games.gazeplayEval.GameState;

import java.util.function.Function;

import static net.gazeplay.games.gazeplayEval.config.Const.*;

public class SelectionProgress extends ProgressIndicator {
    private Timeline animationTimeLine;
    private final double progressDuration;
    private final EventHandler<ActionEvent> onAnimationFinish;

    public SelectionProgress(double duration, double initialX, double initialY, double initialWidth, double initialHeight, Function<Void, Void> onProgressFinish) {
        // progressIndicator de 2cm de diamètre
        super(0);
        this.progressDuration = duration;
        this.setTranslateX(initialX + (initialWidth - SELECTION_PROGRESS_MIN_WIDTH) / 2.0);
        this.setTranslateY(initialY + (initialHeight - SELECTION_PROGRESS_MIN_HEIGHT) / 2.0);
        this.setMinWidth(SELECTION_PROGRESS_MIN_WIDTH);
        this.setMinHeight(SELECTION_PROGRESS_MIN_HEIGHT);
        this.setOpacity(SELECTION_PROGRESS_OPACITY);
        this.setVisible(false);

        onAnimationFinish = (actionEvent) -> {
            this.setVisible(false);
            onProgressFinish.apply(null);
        };
    }

    public void start() {
        this.newAnimationTimeLine();
        this.setStyle(" -fx-progress-color: " + GameState.context.getConfiguration().getProgressBarColor());
        this.setMinWidth(GameState.context.getConfiguration().getProgressBarSize());
        this.setMinHeight(GameState.context.getConfiguration().getProgressBarSize());
        this.setProgress(0);
        this.setVisible(true);
        animationTimeLine.playFromStart();
    }

    public void stop() {
        animationTimeLine.stop();
        this.setVisible(false);
        this.setProgress(0);
    }

    private void newAnimationTimeLine() {
        animationTimeLine = new Timeline();
        animationTimeLine.getKeyFrames().add(new KeyFrame(new Duration(progressDuration), new KeyValue(this.progressProperty(), 1)));
        animationTimeLine.setOnFinished(onAnimationFinish);
    }
}
