package net.gazeplay.games.dottodot;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class DotEntity extends Parent {
    private final IGameContext gameContext;
    private final ProgressIndicator progressIndicator;
    private Timeline progressTimeline;
    private StackPane dotShape;
    private final Stats stats;
    private final DotToDotGameVariant gameVariant;
    private DotToDot gameObject;
    private final int index;
    private boolean isFirst = false;

    @Setter
    @Getter
    private int previous;

    public DotEntity(final StackPane dotShape, final Stats stats,
                     final ProgressIndicator progressIndicator, final Text number, final IGameContext gameContext, final DotToDotGameVariant gameVariant, DotToDot gameInstance, int index) {
        this.gameContext = gameContext;
        this.progressIndicator = progressIndicator;
        this.progressIndicator.setMouseTransparent(true);
        this.dotShape = dotShape;
        this.stats = stats;
        gameObject = gameInstance;
        this.index = index;
        this.gameVariant = gameVariant;

        if (this.index == 1)
            isFirst = true;
        else
            previous = index - 1;

        number.setMouseTransparent(true);
        this.getChildren().addAll(this.dotShape, number, this.progressIndicator);

        final EventHandler<Event> enterHandler = (Event event) -> {
            progressTimeline = new Timeline(
                new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(this.progressIndicator.progressProperty(), 1)));

            progressTimeline.setOnFinished(e -> drawTheLine());

            this.progressIndicator.setOpacity(1);
            progressTimeline.playFromStart();
        };

        this.dotShape.addEventFilter(MouseEvent.MOUSE_ENTERED, enterHandler);
        this.dotShape.addEventFilter(GazeEvent.GAZE_ENTERED, enterHandler);

        final EventHandler<Event> exitHandler = (Event event) -> {
            progressTimeline.stop();
            this.progressIndicator.setOpacity(0);
            this.progressIndicator.setProgress(0);
        };

        this.dotShape.addEventFilter(MouseEvent.MOUSE_EXITED, exitHandler);
        this.dotShape.addEventFilter(GazeEvent.GAZE_EXITED, exitHandler);
    }

    double getXValue() {
        return this.dotShape.getLayoutX() + this.dotShape.getPrefWidth() / 2;
    }

    double getYValue() {
        return this.dotShape.getLayoutY() + this.dotShape.getPrefHeight() / 2;
    }

    public void drawTheLine() {
        if (previous == gameObject.getPrevious()) {
            if (gameVariant.getLabel().contains("Order")
                && !gameContext.getChildren().contains(gameObject.getDotList().get(index))) {
                gameObject.positioningDot(gameObject.getDotList().get(index));

            }

            nextDot(gameObject.getDotList().get(index - 2).getXValue(), gameObject.getDotList().get(index - 2).getYValue(),
                gameObject.getDotList().get(index - 1).getXValue(), gameObject.getDotList().get(index - 1).getYValue());
            gameObject.setPrevious(index);
            dotShape.getChildren().forEach(circle -> {
                ((Circle) circle).setFill(Color.RED);
            });


        } else if (isFirst && gameObject.getPrevious() == 1) {
            dotShape.getChildren().forEach(circle -> {
                ((Circle) circle).setFill(Color.RED);
            });

        } else if (isFirst && gameObject.getPrevious() == gameObject.getDotList().size()) {
            nextDot(gameObject.getDotList().get(gameObject.getDotList().size() - 1).getXValue(), gameObject.getDotList().get(gameObject.getDotList().size() - 1).getYValue(),
                gameObject.getDotList().get(0).getXValue(), gameObject.getDotList().get(0).getYValue());
            dotShape.getChildren().forEach(circle -> {
                ((Circle) circle).setFill(Color.RED);
            });

            stats.incrementNumberOfGoalsReached();
            log.debug("level = {}, nbGoalsReached = {}, fails = {}", gameObject.getLevel(), stats.nbGoalsReached, gameObject.getFails());
            gameObject.getListOfFails().add(gameObject.getFails());
            gameObject.setFails(0);

            if (gameVariant.getLabel().contains("Dynamic")) {
                if (stats.nbGoalsReached > 0 && stats.nbGoalsReached % 3 == 0) {
                    if (nextLevelDecision() && gameObject.getLevel() < 7)
                        gameObject.setLevel(gameObject.getLevel() + 1);

                    if (!nextLevelDecision() && gameObject.getLevel() > 1)
                        gameObject.setLevel(gameObject.getLevel() - 1);
                }
            }

            gameObject.setPrevious(1);

            gameContext.updateScore(stats, gameObject);
            gameObject.getDotList().clear();
            gameContext.playWinTransition(500, actionEvent -> {
                gameObject.dispose();
                gameContext.clear();
                gameObject.launch();
            });

        } else {
            gameObject.catchFail();
        }
    }

    public void nextDot(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStyle("-fx-stroke: indigo;");
        line.setStrokeWidth(5);
        line.setMouseTransparent(true);

        gameObject.getLineList().add(line);
        gameContext.getChildren().add(line);
    }

    public boolean nextLevelDecision() {
        int nbOfGoalsReached = stats.nbGoalsReached;
        int compare = 3;
        for (int i = 0; i < 3; i++) {
            if (gameObject.getListOfFails().get(nbOfGoalsReached - i - 1) > 1)
                compare--;
        }

        return (compare == 3);
    }
}
