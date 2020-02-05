/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Peter Bardawil
 */
@Slf4j
class SlidingPuzzleCard extends Parent {

    private final double fixationlength;

    private final Rectangle card;

    @Getter
    private final int CardId;

    private final IGameContext gameContext;

    private final int initWidth;

    @Getter
    @Setter
    private int initX;

    @Getter
    @Setter
    private int initY;

    @Getter
    @Setter
    private int kingPosX, kingPosY;

    @Getter
    @Setter
    private boolean isKing;

    private final SlidingPuzzle gameInstance;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;

    private final Stats stats;

    /**
     * Use a comma Timeline object so we can stop the current animation to prevent overlapses.
     */
    private Timeline currentTimeline;


    SlidingPuzzleCard(final int id, final double positionX, final double positionY, final double width, final double height, final String fileName,
                      final double fixationlength, final IGameContext gameContext, final SlidingPuzzle gameInstance, final Stats stats, final double kingPosX,
                      final double kingPosY) {
        this.fixationlength = fixationlength;
        this.CardId = id;
        this.card = new Rectangle(positionX, positionY, width, height);
        this.card.setFill(new ImagePattern(new Image(fileName), 0, 0, 1, 1, true));
        this.gameContext = gameContext;
        this.initWidth = (int) width;
        this.initX = (int) positionX;
        this.initY = (int) positionY;
        this.gameInstance = gameInstance;
        this.stats = stats;
        this.progressIndicator = createProgressIndicator(width);
        this.getChildren().add(this.card);
        this.getChildren().add(this.progressIndicator);
        this.isKing = false;
        this.kingPosX = (int) kingPosX;
        this.kingPosY = (int) kingPosY;
        final EventHandler<Event> enterEvent;
        if (id != 9) {
            enterEvent = buildEvent();
        } else {
            enterEvent = buildEvent2();
        }
        gameContext.getGazeDeviceManager().addEventFilter(card);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        // Prevent null pointer exception
        currentTimeline = new Timeline();
    }

    private ProgressIndicator createProgressIndicator(final double width) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(initX);
        indicator.setTranslateY(initY);
        indicator.setMinWidth(width);
        indicator.setMinHeight(width);
        indicator.setOpacity(0);
        indicator.setMouseTransparent(true);
        return indicator;
    }

    private Boolean checkIfNeighbor() {

        if (this.initX == kingPosX && ((this.initY == kingPosY + initWidth) || (this.initY == kingPosY - initWidth))) {
            return true;
        } else {
            return this.initY == kingPosY
                && ((this.initX == kingPosX + initWidth) || (this.initX == kingPosX - initWidth));
        }
    }

    private void isMyNeighborEvent() {

        progressIndicator.setOpacity(0.7);
        // currentTimeline.stop();
        currentTimeline = new Timeline();
        final KeyValue xValue = new KeyValue(card.xProperty(), kingPosX);
        final KeyValue yValue = new KeyValue(card.yProperty(), kingPosY);
        final KeyFrame keyFrame = new KeyFrame(Duration.millis(100), xValue, yValue);
        currentTimeline.getKeyFrames().add(keyFrame);
        currentTimeline.play();

    }

    void isKingCardEvent(final double x, final double y) {
        progressIndicator.setOpacity(0);
        // currentTimeline.stop();
        currentTimeline = new Timeline();
        final KeyValue xValue = new KeyValue(card.xProperty(), x);
        final KeyValue yValue = new KeyValue(card.yProperty(), y);
        final KeyFrame keyFrame = new KeyFrame(Duration.millis(200), xValue, yValue);
        currentTimeline.getKeyFrames().add(keyFrame);
        currentTimeline.play();
    }

    private void onGameOver() {
        stats.incNbGoals();

        progressIndicator.setOpacity(0);

        // currentTimeline.stop();
        // currentTimeline = new Timeline();

        currentTimeline.onFinishedProperty().set(actionEvent -> gameContext.playWinTransition(500, actionEvent1 -> {
            gameInstance.dispose();

            gameContext.clear();

            gameInstance.launch();

            try {
                stats.saveStats();
            } catch (final IOException ex) {
                Logger.getLogger(SlidingPuzzleCard.class.getName()).log(Level.SEVERE, null, ex);
            }

            stats.notifyNewRoundReady();

            gameContext.onGameStarted();
        }));

        if (!currentTimeline.getStatus().equals(Timeline.Status.RUNNING)) {
            currentTimeline.playFromStart();
        }
    }

    private EventHandler<Event> buildEvent() {

        return e -> {

            if (!(currentTimeline.getStatus() == Animation.Status.RUNNING)
                && (e.getEventType() == MouseEvent.MOUSE_ENTERED
                || e.getEventType() == GazeEvent.GAZE_ENTERED) && checkIfNeighbor()) {

                progressIndicator.setOpacity(0.7);
                progressIndicator.setProgress(0);

                // currentTimeline.stop();
                currentTimeline = new Timeline();

                timelineProgressBar = new Timeline();

                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                    new KeyValue(progressIndicator.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {

                    progressIndicator.setTranslateX(kingPosX);
                    progressIndicator.setTranslateY(kingPosY);

                    // gameInstance.showCards();

                    gameInstance.replaceCards(fixationlength, initX, initY, CardId);

                    isMyNeighborEvent();

                    gameInstance.fixCoord(CardId, initX, initY, kingPosX, kingPosY);
                    // gameInstance.showCards();

                    if (gameInstance.isGameOver()) {
                        onGameOver();
                    }

                });

                currentTimeline.play();
                timelineProgressBar.play();
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                timelineProgressBar.stop();
                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }

    private EventHandler<Event> buildEvent2() {

        return e -> {
        };
    }
}
