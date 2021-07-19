package net.gazeplay.games.dottodot;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import javafx.scene.image.ImageView;

@Slf4j
public class DotEntity extends Parent {
    private final IGameContext gameContext;
    private final ProgressIndicator progressIndicator;
    private Timeline progressTimeline;
    private final Stats stats;
    private static DotToDot gameObject;
    private int index;
    private boolean isLast = false;

    @Setter @Getter
    private int previous;

    public DotEntity (final ImageView imageView, final Stats stats,
                      final ProgressIndicator progressIndicator, final IGameContext gameContext, DotToDot gameInstance, int index) {
        this.gameContext = gameContext;
        this.progressIndicator = progressIndicator;
        this.stats = stats;
        this.gameObject = gameInstance;
        this.index = index;

        if (this.index == gameObject.getTargetAOIList().size() - 1)
            isLast = true;

        this.getChildren().addAll(imageView, progressIndicator);

        final EventHandler<Event> enterHandler = (Event event) -> {
            progressTimeline = new Timeline(
                new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(progressIndicator.progressProperty(), 1)));

            //TODO take the previous value into account
            if (!isLast)
                progressTimeline.setOnFinished(e -> nextDot(gameObject.getTargetAOIList().get(index).getXValue(), gameObject.getTargetAOIList().get(index).getYValue(),
                    gameObject.getTargetAOIList().get(index + 1).getXValue(), gameObject.getTargetAOIList().get(index + 1).getYValue()));
            else
                progressTimeline.setOnFinished(e -> nextDot(gameObject.getTargetAOIList().get(index).getXValue(), gameObject.getTargetAOIList().get(index).getYValue(),
                    gameObject.getTargetAOIList().get(0).getXValue(), gameObject.getTargetAOIList().get(0).getYValue()));

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

    public void nextDot(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX + 60, startY, endX + 60, endY);
        line.setStyle("-fx-stroke: red;");
        line.setStrokeWidth(5);

        gameContext.getChildren().add(line);
        log.info("startX = {}, startY = {}, endX = {}, endY = {}", gameObject.getTargetAOIList().get(0).getXValue(), gameObject.getTargetAOIList().get(0).getYValue(),
            gameObject.getTargetAOIList().get(1).getXValue(), gameObject.getTargetAOIList().get(1).getYValue());
    }
}
