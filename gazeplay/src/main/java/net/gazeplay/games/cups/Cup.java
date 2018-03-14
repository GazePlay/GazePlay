package net.gazeplay.games.cups;

import net.gazeplay.games.cups.utils.PositionCup;
import java.awt.Point;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class Cup {

    @Getter
    private final Rectangle item;
    private boolean hasBall;
    @Getter
    @Setter
    private Ball ball;
    @Getter
    @Setter
    private int ballRadius;

    private final int openCupSpeed;
    private final int fixationDurationNeeded;

    @Getter
    private final PositionCup positionCup;

    @Getter
    private final double widthItem;
    @Getter
    private final double heightItem;
    @Getter
    @Setter
    private ProgressIndicator progressIndicator;

    private final GameContext gameContext;

    private final CupsAndBalls gameInstance;

    @Getter
    @Setter
    private boolean revealed = false;

    private Timeline timelineProgressBar;
    private final Stats stats;

    private final EventHandler<Event> enterEvent;
    @Getter
    @Setter
    private boolean winner;
    @Getter
    private int actionsDone;
    @Getter
    @Setter
    private int actionsToDo;

    public Cup(Rectangle item, PositionCup positionCup, GameContext gameContext, Stats stats, CupsAndBalls gameInstance,
            int openCupSpeed) {
        this.actionsDone = 0;
        this.actionsToDo = 0;
        this.openCupSpeed = openCupSpeed;
        this.fixationDurationNeeded = 2000;
        this.item = item;
        this.widthItem = item.getWidth();
        this.heightItem = item.getHeight();
        this.positionCup = positionCup;

        this.gameContext = gameContext;

        this.stats = stats;

        this.gameInstance = gameInstance;

        this.enterEvent = buildEvent();

        this.progressIndicator = createProgressIndicator(item.getX(), item.getY(), this.enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(item);

        item.addEventFilter(MouseEvent.ANY, enterEvent);
        item.addEventFilter(GazeEvent.ANY, enterEvent);
    }

    public boolean containsBall() {
        return hasBall;
    }

    public void giveBall(boolean hasBall) {
        this.hasBall = hasBall;
    }

    public void updatePosition(int newCellX, int newCellY) {
        Point newPosCup = positionCup.calculateXY(newCellX, newCellY);
        item.setX(newPosCup.getX());
        item.setY(newPosCup.getY());
    }

    public void progressBarUpdatePosition(double newXCupPos, double newYCupPos) {
        gameContext.getChildren().remove(progressIndicator);
        progressIndicator = createProgressIndicator(newXCupPos, newYCupPos, enterEvent);
        progressIndicator.toFront();
    }

    private ProgressIndicator createProgressIndicator(double xCupPos, double yCupPos, EventHandler<Event> enterEvent) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        double indicatorWidth = item.getWidth() * 0.645;
        double indicatorHeight = item.getHeight() * 0.645;
        indicator.setPrefSize(indicatorWidth, indicatorHeight);
        indicator.setTranslateX(xCupPos + (item.getWidth() - indicatorWidth) / 2);
        indicator.setTranslateY(yCupPos + (item.getHeight() - indicatorHeight) / 2);
        indicator.setOpacity(0);
        gameContext.getGazeDeviceManager().addEventFilter(indicator);

        indicator.addEventFilter(MouseEvent.ANY, enterEvent);
        indicator.addEventFilter(GazeEvent.ANY, enterEvent);

        gameContext.getChildren().add(indicator);
        return indicator;
    }

    private void onCorrectCupSelected() {
        stats.incNbGoals();

        progressIndicator.setOpacity(0);

        gameInstance.openAllIncorrectCups();

        TranslateTransition revealBallTransition = new TranslateTransition(Duration.millis(openCupSpeed), item);
        revealBallTransition.setByY(-ballRadius * 8);
        ball.getItem().setVisible(true);

        revealBallTransition.onFinishedProperty().set((EventHandler<ActionEvent>) (ActionEvent actionEvent) -> {
            gameContext.playWinTransition(2000, (ActionEvent actionEvent1) -> {
                gameInstance.dispose();

                gameContext.clear();

                gameInstance.launch();

                gameContext.onGameStarted();
            });
        });

        revealBallTransition.play();
    }

    private void onWrongCupSelected() {
        progressIndicator.setOpacity(0);
        TranslateTransition revealBallTransition = new TranslateTransition(Duration.millis(openCupSpeed), item);

        revealBallTransition.setByY(-ballRadius * 8);

        revealBallTransition.play();
    }

    private EventHandler<Event> buildEvent() {
        return (Event e) -> {
            if (revealed || actionsDone < actionsToDo - 1)
                return;

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                progressIndicator.setOpacity(1);
                progressIndicator.setProgress(0);

                timelineProgressBar = new Timeline();

                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationDurationNeeded),
                        new KeyValue(progressIndicator.progressProperty(), 1)));

                timelineProgressBar.play();

                timelineProgressBar.setOnFinished((ActionEvent actionEvent) -> {
                    revealed = true;

                    item.removeEventFilter(MouseEvent.ANY, enterEvent);
                    item.removeEventFilter(GazeEvent.ANY, enterEvent);

                    if (winner) {
                        onCorrectCupSelected();
                    } else {
                        onWrongCupSelected();
                    }
                });
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                timelineProgressBar.stop();

                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }

    public void increaseActionsDone() {
        actionsDone++;
    }
}
