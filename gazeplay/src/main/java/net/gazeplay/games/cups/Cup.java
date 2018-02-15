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
import javafx.event.EventType;
import javafx.scene.Parent;
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
public class Cup extends Parent {

    @Getter
    private final Rectangle item;
    private boolean hasBall;
    @Getter
    @Setter
    private Ball ball;

    @Getter
    private PositionCup positionCup;

    @Getter
    private final double widthItem;
    @Getter
    private final double heightItem;
    @Getter
    @Setter
    private ProgressIndicator progressIndicator;

    /* Taken from Magic Cards */
    private final GameContext gameContext;

    private final CupsAndBalls gameInstance;

    private boolean revealed = false;

    private Timeline timelineProgressBar;
    private final Stats stats;

    final EventHandler<Event> enterEvent;
    @Getter
    @Setter
    boolean winner;

    public Cup(Rectangle item, PositionCup positionCup, GameContext gameContext, Stats stats,
            CupsAndBalls gameInstance) {
        this.item = item;
        this.widthItem = item.getWidth();
        this.heightItem = item.getHeight();
        this.positionCup = positionCup;

        this.gameContext = gameContext;

        this.stats = stats;

        this.gameInstance = gameInstance;

        this.progressIndicator = createProgressIndicator(item.getX(), item.getY());
        progressIndicator.toFront();

        this.enterEvent = buildEvent();

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
        progressIndicator = createProgressIndicator(newXCupPos, newYCupPos);
        progressIndicator.toFront();
    }

    private ProgressIndicator createProgressIndicator(double xCupPos, double yCupPos) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        double indicatorWidth = item.getWidth() * 0.645;
        double indicatorHeight = item.getHeight() * 0.645;
        indicator.setPrefSize(indicatorWidth, indicatorHeight);
        indicator.setTranslateX(xCupPos + (item.getWidth() - indicatorWidth) / 2);
        indicator.setTranslateY(yCupPos + (item.getHeight() - indicatorHeight) / 2);
        indicator.setOpacity(0);
        gameContext.getChildren().add(indicator);
        return indicator;
    }

    private void onCorrectCupSelected() {
        stats.incNbGoals();

        progressIndicator.setOpacity(0);

        // gameInstance.openAllIncorrectCups();

        TranslateTransition revealBallTransition = new TranslateTransition(Duration.millis(1000/* 3000 */), item);
        revealBallTransition.setByY(-ball.getRadius() * 8);

        /*
         * revealBallTransition.onFinishedProperty().set(new EventHandler<ActionEvent>() {
         * 
         * @Override public void handle(ActionEvent actionEvent) {
         * 
         * gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {
         * 
         * @Override public void handle(ActionEvent actionEvent) { gameInstance.dispose();
         * 
         * gameContext.clear();
         * 
         * gameInstance.launch();
         * 
         * stats.start();
         * 
         * gameContext.onGameStarted(); } }); } });
         */

        revealBallTransition.play();
    }

    private void onWrongCupSelected() {

        TranslateTransition revealBallTransition = new TranslateTransition(Duration.millis(1000/* 3000 */), item);
        revealBallTransition.setByY(-ball.getRadius() * 8);

        revealBallTransition.setOnFinished(e -> {
            item.setVisible(false);
        });
        revealBallTransition.play();
        progressIndicator.setOpacity(0);
    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (revealed)
                    return;

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    log.info("ENTERED");
                    progressIndicator.setOpacity(1);
                    progressIndicator.setProgress(0);

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames().add(
                            new KeyFrame(new Duration(2000), new KeyValue(progressIndicator.progressProperty(), 1)));

                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {

                            revealed = true;

                            item.removeEventFilter(MouseEvent.ANY, enterEvent);
                            item.removeEventFilter(GazeEvent.ANY, enterEvent);

                            if (winner) {
                                onCorrectCupSelected();
                            } else {
                                onWrongCupSelected();
                            }
                        }
                    });
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                    log.info("EXITED");
                    timelineProgressBar.stop();

                    progressIndicator.setOpacity(0);
                    progressIndicator.setProgress(0);
                }
            }
        };
    }
}
