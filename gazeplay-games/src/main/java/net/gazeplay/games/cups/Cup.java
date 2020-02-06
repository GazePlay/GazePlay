package net.gazeplay.games.cups;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.cups.utils.PositionCup;

import java.awt.*;

@Slf4j
public class Cup {

    @Getter
    private final ImageView item;
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

    private final IGameContext gameContext;

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

    public Cup(final ImageView item, final PositionCup positionCup, final IGameContext gameContext, final Stats stats, final CupsAndBalls gameInstance,
               final int openCupSpeed) {
        this.actionsDone = 0;
        this.actionsToDo = 0;
        this.openCupSpeed = openCupSpeed;
        this.fixationDurationNeeded = gameContext.getConfiguration().getFixationLength();
        this.item = item;
        this.widthItem = item.getFitWidth();
        this.heightItem = item.getFitHeight();
        this.positionCup = positionCup;

        this.gameContext = gameContext;

        this.stats = stats;

        this.gameInstance = gameInstance;

        this.enterEvent = buildEvent();

        this.progressIndicator = createProgressIndicator(item.getX(), item.getY());

        createEvent();

    }

    public void createEvent() {

        this.item.addEventFilter(MouseEvent.ANY, enterEvent);
        this.item.addEventFilter(GazeEvent.ANY, enterEvent);

        this.gameContext.getGazeDeviceManager().addEventFilter(this.item);
    }

    public boolean containsBall() {
        return hasBall;
    }

    public void giveBall(final boolean hasBall) {
        this.hasBall = hasBall;
    }

    public void updatePosition(final int newCellX, final int newCellY) {
        final Point newPosCup = positionCup.calculateXY(newCellX, newCellY);
        item.setX(newPosCup.getX());
        item.setY(newPosCup.getY());
    }

    public void progressBarUpdatePosition(final double newXCupPos, final double newYCupPos) {
        gameContext.getChildren().remove(progressIndicator);
        progressIndicator = createProgressIndicator(newXCupPos, newYCupPos);
        progressIndicator.toFront();
    }

    private ProgressIndicator createProgressIndicator(final double xCupPos, final double yCupPos) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        final double indicatorWidth = item.getFitWidth() * 0.645;
        final double indicatorHeight = item.getFitHeight() * 0.645;
        indicator.setPrefSize(indicatorWidth, indicatorHeight);
        indicator.setTranslateX(xCupPos + (item.getFitWidth() - indicatorWidth) / 2);
        indicator.setTranslateY(yCupPos + (item.getFitHeight() - indicatorHeight) / 2);
        indicator.setOpacity(0);
        indicator.addEventFilter(MouseEvent.ANY, enterEvent);
        indicator.addEventFilter(GazeEvent.ANY, enterEvent);

        /* gameContext.getGazeDeviceManager().addEventFilter(indicator); */

        gameContext.getChildren().add(indicator);
        return indicator;
    }

    private void onCorrectCupSelected() {
        stats.incNbGoals();

        progressIndicator.setOpacity(0);

        gameInstance.openAllIncorrectCups();

        final TranslateTransition revealBallTransition = new TranslateTransition(Duration.millis(openCupSpeed), item);
        revealBallTransition.setByY(-ballRadius * 8);
        ball.getItem().setVisible(true);

        revealBallTransition.setOnFinished((ActionEvent actionEvent) -> gameContext.playWinTransition(2000, (ActionEvent actionEvent1) -> {
            gameInstance.dispose();

            gameContext.clear();

            gameInstance.launch();

            gameContext.onGameStarted();
        }));

        revealBallTransition.play();
    }

    private void onWrongCupSelected() {
        progressIndicator.setOpacity(0);
        final TranslateTransition revealBallTransition = new TranslateTransition(Duration.millis(openCupSpeed), item);

        revealBallTransition.setByY(-ballRadius * 8);

        revealBallTransition.play();
    }

    private EventHandler<Event> buildEvent() {
        return (Event e) -> {

            if (revealed || actionsDone < actionsToDo - 1) {
                return;
            }

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                progressIndicator.setOpacity(1);
                progressIndicator.setProgress(0);

                timelineProgressBar = new Timeline();

                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationDurationNeeded),
                    new KeyValue(progressIndicator.progressProperty(), 1)));

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

                timelineProgressBar.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                if (timelineProgressBar != null) {
                    timelineProgressBar.stop();
                }

                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }

    public void increaseActionsDone() {
        actionsDone++;
    }
}
