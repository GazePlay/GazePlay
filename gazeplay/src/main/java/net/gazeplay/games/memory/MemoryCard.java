package net.gazeplay.games.memory;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class MemoryCard extends Parent {

    private final double fixationlength;

    private final Rectangle card;

    private final Image image;

    private final int id;

    private final GameContext gameContext;

    private final Memory gameInstance;

    /**
     * true if the card has been turned
     */
    private boolean turned;

    private int cardAlreadyTurned;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;

    final Stats stats;

    final EventHandler<Event> enterEvent;

    public MemoryCard(double positionX, double positionY, double width, double height, Image image, int idc,
            GameContext gameContext, Stats stats, Memory gameInstance, int fixationlength) {

        this.card = new Rectangle(positionX, positionY, width, height);

        this.card.setFill(new ImagePattern(new Image("data/magiccards/images/red-card-game.png"), 0, 0, 1, 1, true));

        this.image = image;

        this.id = idc;

        this.gameContext = gameContext;

        this.stats = stats;

        this.turned = false;

        this.cardAlreadyTurned = -1;

        this.fixationlength = fixationlength;

        this.gameInstance = gameInstance;

        this.getChildren().add(card);

        this.progressIndicator = createProgressIndicator(width, height);
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(card);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);
    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX() + width * 0.05);
        indicator.setTranslateY(card.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onCorrectCardSelected() {

        stats.incNbGoals();

        for (int i = 0; i < gameInstance.currentRoundDetails.cardList.size(); i++) {
            gameInstance.currentRoundDetails.cardList.get(i).cardAlreadyTurned = -1;
            if (gameInstance.currentRoundDetails.cardList.get(i).turned == true) {
                gameInstance.currentRoundDetails.cardList.get(i).card.removeEventFilter(MouseEvent.ANY, enterEvent);
                gameInstance.currentRoundDetails.cardList.get(i).card.removeEventFilter(GazeEvent.ANY, enterEvent);
                gameContext.getGazeDeviceManager()
                        .removeEventFilter(gameInstance.currentRoundDetails.cardList.get(i).card);

            }
        }
        progressIndicator.setOpacity(0);

        gameInstance.nbTurnedCards = 0;

        gameInstance.removeSelectedCards();

        /* No more cards to play : End of this game : Begin a new Game */
        if (gameInstance.getnbRemainingPeers() == 0) {

            gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent actionEvent) {

                    gameInstance.dispose();

                    gameContext.clear();

                    gameInstance.launch();

                    stats.start();

                    gameContext.onGameStarted();
                }
            });
        }
    }

    /* The 2 turned cards are not matching */
    private void onWrongCardSelected() {

        Timeline timeline = new Timeline();

        /* No cards are turned now */
        for (int i = 0; i < gameInstance.currentRoundDetails.cardList.size(); i++) {
            if (gameInstance.currentRoundDetails.cardList.get(i).turned == true) {
                gameInstance.currentRoundDetails.cardList.get(i).turned = false;
                gameInstance.currentRoundDetails.cardList.get(i).progressIndicator.setOpacity(0);
                gameInstance.currentRoundDetails.cardList.get(i).card.setFill(
                        new ImagePattern(new Image("data/magiccards/images/red-card-game.png"), 0, 0, 1, 1, true));
            }
            gameInstance.currentRoundDetails.cardList.get(i).cardAlreadyTurned = -1;
        }
        gameInstance.nbTurnedCards = 0;
        timeline.play();

    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (turned)
                    return;
                if (gameInstance.nbTurnedCards == 2) {
                    return;
                }
                /* First card */
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    if (cardAlreadyTurned == -1) {

                        progressIndicator.setOpacity(1);
                        progressIndicator.setProgress(0);

                        timelineProgressBar = new Timeline();

                        timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                                new KeyValue(progressIndicator.progressProperty(), 1)));

                        timelineProgressBar.play();
                        timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {

                            @Override
                            public void handle(ActionEvent actionEvent) {
                                gameInstance.nbTurnedCards = gameInstance.nbTurnedCards + 1;
                                turned = true;
                                card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));

                                /* Update the cardAlreadyTurned for all other cards */
                                for (int i = 0; i < gameInstance.currentRoundDetails.cardList.size(); i++) {
                                    gameInstance.currentRoundDetails.cardList.get(i).cardAlreadyTurned = id;
                                }
                                progressIndicator.setOpacity(0);

                            }
                        });
                    } else { /* 2nd card */

                        progressIndicator.setOpacity(1);
                        progressIndicator.setProgress(0);

                        timelineProgressBar = new Timeline();

                        timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                                new KeyValue(progressIndicator.progressProperty(), 1)));

                        timelineProgressBar.play();

                        timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {

                                gameInstance.nbTurnedCards = gameInstance.nbTurnedCards + 1;
                                turned = true;
                                card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
                                progressIndicator.setOpacity(0);

                                /* Timeline : To see the 2nd card */
                                Timeline timelineCard = new Timeline();

                                timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1000)));

                                timelineCard.play();

                                timelineCard.setOnFinished(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {

                                        if (timelineCard != null)
                                            timelineCard.stop();

                                        if (id == cardAlreadyTurned) {
                                            onCorrectCardSelected();
                                        } else {
                                            onWrongCardSelected();
                                        }
                                    }
                                });

                            }
                        });
                    }

                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    Timeline timeline = new Timeline();

                    timeline.play();
                    if (timelineProgressBar != null)
                        timelineProgressBar.stop();

                    progressIndicator.setOpacity(0);
                    progressIndicator.setProgress(0);
                }
            }
        };
    }

    public boolean isTurned() {
        return turned;
    }

    public void setImageDejaRetournee(int id) {
        this.cardAlreadyTurned = id;
    }

}