package net.gazeplay.games.memory;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.LevelsReport;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class MemoryCard extends Parent {

    private final double fixationlength;

    private final Rectangle card;

    private final Image image;

    private final int id;

    private final IGameContext gameContext;

    private final Memory gameInstance;

    /**
     * true if the card has been turned
     */
    private boolean turned;

    private int cardAlreadyTurned;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;

    final Stats stats;

    private final boolean inReplayMode;

    final EventHandler<Event> enterEvent;

    final boolean isOpen;

    private LevelsReport levelsReport;

    private boolean mouseIsOverCard;


    public MemoryCard(final double positionX, final double positionY, final double width, final double height,
                      final Image image, final int idc, final IGameContext gameContext, final Stats stats,
                      final Memory gameInstance, final int fixationlength, final boolean isOpen, final boolean inReplayMode) {
        this.isOpen = isOpen;

        this.card = new Rectangle(positionX, positionY, width, height);

        if (isOpen) {
            this.card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
        } else {
            this.card
                .setFill(new ImagePattern(new Image("data/magiccards/images/red-card-game.png"), 0, 0, 1, 1, true));
        }

        this.image = image;

        this.id = idc;

        this.gameContext = gameContext;

        this.stats = stats;

        this.inReplayMode = inReplayMode;

        this.turned = false;

        this.cardAlreadyTurned = -1;

        this.fixationlength = fixationlength;

        this.gameInstance = gameInstance;

        this.levelsReport = stats.getLevelsReport();

        this.getChildren().add(card);

        this.progressIndicator = createProgressIndicator(width, height);
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(card);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        // Escape Key to end game
        this.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.SPACE) {
                gameInstance.dispose();
                gameContext.clear();
                ke.consume();
            }
        });

        this.mouseIsOverCard = false;
    }

    private ProgressIndicator createProgressIndicator(final double width, final double height) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX() + width * 0.05);
        indicator.setTranslateY(card.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onCorrectCardSelected() {

        gameInstance.incNbCorrectCards();
        log.debug("nbCorrect = {}", gameInstance.getNbCorrectCards());

        if (!inReplayMode) {
            stats.incrementNumberOfGoalsReached();
        }

        for (int i = 0; i < gameInstance.currentRoundDetails.cardList.size(); i++) {
            if (gameInstance.currentRoundDetails.cardList.get(i).turned && gameInstance.currentRoundDetails.cardList.get(i).id == gameInstance.currentRoundDetails.cardList.get(i).cardAlreadyTurned) {
                gameInstance.currentRoundDetails.cardList.get(i).card.removeEventFilter(MouseEvent.ANY, enterEvent);
                gameInstance.currentRoundDetails.cardList.get(i).card.removeEventFilter(GazeEvent.ANY, enterEvent);
                gameContext.getGazeDeviceManager()
                    .removeEventFilter(gameInstance.currentRoundDetails.cardList.get(i).card);

            } else if (gameInstance.currentRoundDetails.cardList.get(i).turned && !isOpen) {
                gameInstance.currentRoundDetails.cardList.get(i).turned = false;
                gameInstance.currentRoundDetails.cardList.get(i).card
                    .setFill(new ImagePattern(new Image("data/magiccards/images/red-card-game.png"), 0, 0, 1, 1, true));
            }
            gameInstance.currentRoundDetails.cardList.get(i).cardAlreadyTurned = -1;
        }
        progressIndicator.setOpacity(0);

        gameInstance.nbTurnedCards = 0;

        gameInstance.removeSelectedCards();

        /* No more cards to play : End of this game : Begin a new Game */
        if (gameInstance.getNbRemainingPeers() == 0) {
            if (gameInstance.getDifficulty().equals("Dynamic")) {
                levelsReport.addRoundLevel(gameInstance.getLevel());
                gameInstance.addRoundResult(gameInstance.totalNbOfTries());
                int sizeOfList = gameInstance.getListOfResults().size();
                int compare = 0;

                log.debug("nbOfTries = {}", gameInstance.totalNbOfTries());
                if (sizeOfList % 3 == 0 && sizeOfList != 0) {
                    for (int i = 0; i < 3; i++) {
                        if (gameInstance.totalNbOfTries() <= 2 * gameInstance.getLevel() && gameInstance.getNbColumns() <= 6) {
                            compare++;
                        }
                        if (gameInstance.totalNbOfTries() >= 2.5 * gameInstance.getLevel() && gameInstance.getNbColumns() > 2) {
                            compare--;
                        }
                    }
                    if (compare == 3) {
                        if (gameInstance.getLevel() == 6) {
                            gameInstance.setLevel(gameInstance.getLevel() + 2);
                        } else {
                            gameInstance.setLevel(gameInstance.getLevel() + 1);
                        }
                    }
                    if (compare == -3) {
                        if (gameInstance.getLevel() == 8) {
                            gameInstance.setLevel(gameInstance.getLevel() - 2);
                        } else {
                            gameInstance.setLevel(gameInstance.getLevel() - 1);
                        }
                    }
                    levelsReport.addRoundLevel(gameInstance.getLevel());
                }

                gameInstance.adaptLevel();
            }

            gameContext.updateScore(stats, gameInstance);

            gameContext.playWinTransition(500, actionEvent -> {

                gameInstance.resetNbCorrectCards();

                gameInstance.resetNbWrongCards();

                gameInstance.dispose();

                gameContext.clear();

                gameInstance.launch();
            });
        }
    }

    /* The 2 turned cards are not matching */
    private void onWrongCardSelected() {

        gameInstance.incNbWrongCards();
        log.debug("nbWrong = {}", gameInstance.getNbWrongCards());

        if (gameInstance.currentRoundDetails == null) {
            return;
        }

        /* No cards are turned now */
        for (int i = 0; i < gameInstance.currentRoundDetails.cardList.size(); i++) {
            if (gameInstance.currentRoundDetails.cardList.get(i).turned) {
                gameInstance.currentRoundDetails.cardList.get(i).turned = false;
                gameInstance.currentRoundDetails.cardList.get(i).progressIndicator.setOpacity(0);
                if (!isOpen) {
                    gameInstance.currentRoundDetails.cardList.get(i).card.setFill(
                        new ImagePattern(new Image("data/magiccards/images/red-card-game.png"), 0, 0, 1, 1, true));
                }
            }
            gameInstance.currentRoundDetails.cardList.get(i).cardAlreadyTurned = -1;
        }
        gameInstance.nbTurnedCards = 0;

        // Added, basically it runs a new timelineProgressBar when no cards are turned but the cursor is still on the card.
        if (this.mouseIsOverCard) {
            if (timelineProgressBar != null) {
                timelineProgressBar.stop();
            }

            progressIndicator.setOpacity(1);
            progressIndicator.setProgress(0);

            timelineProgressBar = new Timeline();

            timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(this.gameContext.getConfiguration().getFixationLength()),
                new KeyValue(progressIndicator.progressProperty(), 1)));

            timelineProgressBar.setOnFinished(actionEventInside -> {

                gameInstance.nbTurnedCards = gameInstance.nbTurnedCards + 1;
                turned = true;
                if (!isOpen) {
                    card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
                }

                /* Update the cardAlreadyTurned for all other cards */
                for (int i = 0; i < gameInstance.currentRoundDetails.cardList.size(); i++) {
                    gameInstance.currentRoundDetails.cardList.get(i).cardAlreadyTurned = id;
                }
                progressIndicator.setOpacity(isOpen ? 0.35 : 0);
            });


            timelineProgressBar.play();
        }
    }

    private EventHandler<Event> buildEvent() {
        return e -> {

            if (turned) {
                // If the cursor was on a card and then moved away while 2 cards are being turned, the mouse is no longer above the card.
                if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                    this.mouseIsOverCard = false;
                }
                return;
            }
            if (gameInstance.nbTurnedCards == 2) {
                return;
            }
            /* First card */
            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                this.mouseIsOverCard = true;
                if (timelineProgressBar != null) {
                    timelineProgressBar.stop();
                }

                progressIndicator.setOpacity(1);
                progressIndicator.setProgress(0);

                timelineProgressBar = new Timeline();

                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(this.gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(progressIndicator.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {

                    if (cardAlreadyTurned == -1) { /* 1st card */
                        gameInstance.nbTurnedCards = gameInstance.nbTurnedCards + 1;
                        turned = true;
                        if (!isOpen) {
                            card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
                        }

                        /* Update the cardAlreadyTurned for all other cards */
                        for (int i = 0; i < gameInstance.currentRoundDetails.cardList.size(); i++) {
                            gameInstance.currentRoundDetails.cardList.get(i).cardAlreadyTurned = id;
                        }
                        progressIndicator.setOpacity(isOpen ? 0.35 : 0);

                    } else { /* 2nd card */
                        gameInstance.nbTurnedCards = gameInstance.nbTurnedCards + 1;
                        turned = true;
                        if (!isOpen) {
                            card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
                        }
                        progressIndicator.setOpacity(0);

                        if (isOpen) {
                            if (id == cardAlreadyTurned) {
                                onCorrectCardSelected();
                            } else {
                                onWrongCardSelected();
                            }
                        } else {
                            /* Timeline : To see the 2nd card */
                            final Timeline timelineCard = new Timeline();

                            timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1000)));

                            timelineCard.setOnFinished(actionEvent1 -> {

                                if (timelineCard != null) {
                                    timelineCard.stop();
                                }

                                if (id == cardAlreadyTurned) {
                                    onCorrectCardSelected();
                                } else {
                                    onWrongCardSelected();
                                }
                            });
                            timelineCard.play();
                        }
                    }

                });

                timelineProgressBar.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                this.mouseIsOverCard = false;
                if (timelineProgressBar != null) {
                    timelineProgressBar.stop();
                }
                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }


    public boolean isTurned() {
        return turned;
    }

    public Rectangle getImageRectangle() {
        return this.card;
    }

}
