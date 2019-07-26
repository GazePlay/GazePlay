package net.gazeplay.games.egg;

import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Created by schwab on 17/09/2016.
 */
@Slf4j
public class Egg extends Parent {

    private final double fixationlength;

    private final Rectangle card;

    private final GameContext gameContext;

    private final EggGame gameInstance;

    /**
     * true if the card has been turned
     */
    private int turned = 0;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;
    final Stats stats;

    final EventHandler<Event> enterEvent;

    /**
     * Use a comme Timeline object so we can stop the current animation to prevent overlapses.
     */
    private Timeline currentTimeline;

    public Egg(double positionX, double positionY, double width, double height, GameContext gameContext, Stats stats,
            EggGame gameInstance, int fixationlength) {

        this.card = new Rectangle(positionX, positionY, width, height);
        this.card.setFill(new ImagePattern(new Image("data/egg/images/egg1.jpg"), 0, 0, 1, 1, true));

        this.gameContext = gameContext;

        this.stats = stats;

        this.fixationlength = fixationlength;

        this.gameInstance = gameInstance;

        this.getChildren().add(card);

        this.progressIndicator = createProgressIndicator(width / 2, height / 2);
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(card);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        // Prevent null pointer exception
        currentTimeline = new Timeline();
    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX() + width / 2);
        indicator.setTranslateY(card.getY() + height / 2);
        indicator.setMinWidth(width);
        indicator.setMinHeight(width);
        indicator.setOpacity(0);
        return indicator;
    }

    private EventHandler<Event> buildEvent() {

        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    if (turned <= 1) {

                        progressIndicator.setOpacity(0.5);
                        progressIndicator.setProgress(0);

                        currentTimeline.stop();
                        currentTimeline = new Timeline(500);

                        timelineProgressBar = new Timeline();

                        timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                                new KeyValue(progressIndicator.progressProperty(), 1)));

                        timelineProgressBar.play();

                        timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {

                            @Override
                            public void handle(ActionEvent actionEvent) {

                                if (turned == 0) {

                                    card.setFill(
                                            new ImagePattern(new Image("data/egg/images/egg2.jpg"), 0, 0, 1, 1, true));
                                    turned = 1;
                                    stats.incNbGoals();
                                    playSound(2);

                                } else if (turned == 1) {
                                    card.setFill(
                                            new ImagePattern(new Image("data/egg/images/egg3.jpg"), 0, 0, 1, 1, true));
                                    progressIndicator.setOpacity(0);
                                    stats.notifyNewRoundReady();
                                    stats.incNbGoals();
                                    playSound(1);

                                    PauseTransition t = new PauseTransition(Duration.seconds(2));
                                    t.setOnFinished(new EventHandler<ActionEvent>() {

                                        @Override
                                        public void handle(ActionEvent actionEvent) {

                                            Utils.stopSound();

                                            gameInstance.dispose();

                                            try {
                                                gameContext.showRoundStats(stats, gameInstance);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });
                                    t.play();

                                }
                            };
                        });
                    }
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    currentTimeline.stop();
                    currentTimeline = new Timeline();
                    currentTimeline.play();
                    timelineProgressBar.stop();
                    progressIndicator.setOpacity(0);
                    progressIndicator.setProgress(0);
                }
            }
        };
    }

    public void playSound(int i) {
        String soundResource = "data/egg/sounds/" + i + ".mp3";
        try {
            Utils.playSound(soundResource);
        } catch (Exception e) {

            log.warn("file doesn't exist : {}", soundResource);
            log.warn(e.getMessage());
        }
    }

}
