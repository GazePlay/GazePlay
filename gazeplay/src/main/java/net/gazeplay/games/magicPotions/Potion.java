package net.gazeplay.games.magicPotions;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;
import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.io.IOException;
import java.security.Key;
import java.util.LinkedList;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.cups.utils.Action;

/**
 *
 * @author Johana MARKU
 *
 */

@Slf4j
public class Potion extends Parent {

    private final double fixationLength;
    @Getter
    private final Rectangle potion;
    @Getter
    private Color potionColor;
    @Getter
    private final Image image;
    @Setter
    @Getter
    private static LinkedList<Color> mixture = new LinkedList<>(); // what we select to mix we put it in this list

    /**
     * true if the potion has been used/chosen for the mixture
     */
    @Setter
    @Getter
    private boolean chosen;

    private final ProgressIndicator progressIndicator;
    private Timeline timelineProgressBar; // used to make a selection long = fixation length

    private final MagicPotions gameInstance;

    private final GameContext gameContext;

    final MagicPotionsStats stats;
    @Getter
    final EventHandler<Event> enterEvent;

    private Timeline currentTimeline;

    public Potion(double positionX, double positionY, double width, double height, Image image, Color color,
            GameContext gameContext, Stats stats, MagicPotions gameInstance, int fixationlength) {
        this.potion = new Rectangle((int) positionX, (int) positionY, (int) width, (int) height);
        this.potion.setFill(new ImagePattern(image, 0, 0, 1, 1, true));

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setWidth(10);
        shadow.setHeight(10);
        shadow.setOffsetX(5);
        shadow.setOffsetY(5);
        shadow.setRadius(3);
        this.potion.setEffect(shadow);

        this.image = image;
        this.potionColor = color;

        this.chosen = false;
        this.gameContext = gameContext;
        this.stats = (MagicPotionsStats) stats;
        this.gameInstance = gameInstance;
        this.fixationLength = fixationlength;

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(potion);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        currentTimeline = new Timeline();
        this.getChildren().add(this.potion);

        this.progressIndicator = createProgressIndicator(width);
        this.getChildren().add(this.progressIndicator);
    }

    private ProgressIndicator createProgressIndicator(double width) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(potion.getX());
        indicator.setTranslateY(potion.getY() + width * 0.75);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onMixAchieved() {

        MagicPotions.getMixPotColor().setFill(MagicPotions.getColorRequest());
        gameInstance.getPotionBlue().removeEventFilter(MouseEvent.ANY, gameInstance.getPotionBlue().getEnterEvent());
        gameInstance.getPotionBlue().removeEventFilter(GazeEvent.ANY, gameInstance.getPotionBlue().getEnterEvent());
        gameInstance.getPotionRed().removeEventFilter(MouseEvent.ANY, gameInstance.getPotionRed().getEnterEvent());
        gameInstance.getPotionRed().removeEventFilter(GazeEvent.ANY, gameInstance.getPotionRed().getEnterEvent());
        gameInstance.getPotionYellow().removeEventFilter(MouseEvent.ANY,
                gameInstance.getPotionYellow().getEnterEvent());
        gameInstance.getPotionYellow().removeEventFilter(GazeEvent.ANY, gameInstance.getPotionYellow().getEnterEvent());
        stats.incNbGoals();
        currentTimeline.stop();
        currentTimeline = new Timeline();

        currentTimeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                gameContext.playWinTransition(0, new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        gameInstance.dispose();
                        gameContext.clear();
                        gameInstance.launch();
                        stats.notifyNewRoundReady();
                    }
                });
            }
        });
        currentTimeline.play();
    }

    private void onWrongPotionSelected() {

        gameInstance.getPotionBlue().removeEventFilter(MouseEvent.ANY, gameInstance.getPotionBlue().getEnterEvent());
        gameInstance.getPotionBlue().removeEventFilter(GazeEvent.ANY, gameInstance.getPotionBlue().getEnterEvent());
        gameInstance.getPotionRed().removeEventFilter(MouseEvent.ANY, gameInstance.getPotionRed().getEnterEvent());
        gameInstance.getPotionRed().removeEventFilter(GazeEvent.ANY, gameInstance.getPotionRed().getEnterEvent());
        gameInstance.getPotionYellow().removeEventFilter(MouseEvent.ANY,
                gameInstance.getPotionYellow().getEnterEvent());
        gameInstance.getPotionYellow().removeEventFilter(GazeEvent.ANY, gameInstance.getPotionYellow().getEnterEvent());
        progressIndicator.setStyle(" -fx-progress-color: red;");
        progressIndicator.setOpacity(1);
        progressIndicator.setAccessibleText("");

        currentTimeline.stop();
        currentTimeline = new Timeline();

        Explosion exp = new Explosion(gameContext.getGamePanelDimensionProvider().getDimension2D());
        gameContext.getChildren().add(exp);
        gameContext.getChildren().removeAll(MagicPotions.getMixPot(), MagicPotions.getMixPotColor());

        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(4000), new KeyValue(exp.opacityProperty(), 0)));

        currentTimeline.play();
        currentTimeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                gameInstance.dispose();
                gameContext.clear();
                gameInstance.launch();
                try {
                    stats.saveStats();
                } catch (IOException ex) {
                    log.info("Io exception");
                }

                stats.notifyNewRoundReady();
            }
        });
    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if (chosen)
                    return;
                if (event.getEventType() == MouseEvent.MOUSE_ENTERED
                        || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                    progressIndicator.setOpacity(1);
                    progressIndicator.setProgress(0);

                    currentTimeline.stop();
                    currentTimeline = new Timeline();

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationLength),
                            new KeyValue(progressIndicator.progressProperty(), 1)));

                    currentTimeline.play();

                    timelineProgressBar.play();

                    chosen = true;
                    mixture.add(potionColor); // we add the color of the potion to our mixture

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {

                            // change opacity of potion when it has been selected once
                            potion.setOpacity(.3);

                            potion.removeEventFilter(MouseEvent.ANY, enterEvent); // we cannot select anymore this color
                            potion.removeEventFilter(GazeEvent.ANY, enterEvent);

                            if (!gameInstance.currentRoundDetails.getPotionsToMix().contains(potionColor)) {
                                /**
                                 * this color is not part of the mixture
                                 */
                                onWrongPotionSelected();
                            } else if (mixture.containsAll(gameInstance.currentRoundDetails.getPotionsToMix())) {
                                /**
                                 * we have achieved the correct mixture
                                 */
                                onMixAchieved();
                            } else {
                                stats.incNbGoals();
                            }
                            switch (mixture.size()) {
                            case 1:
                                MagicPotions.getMixPotColor().setFill(mixture.get(0));
                                break;
                            case 2:
                                if (gameInstance.currentRoundDetails.getPotionsToMix().size() == 2)
                                    MagicPotions.getMixPotColor()
                                            .setFill(gameInstance.currentRoundDetails.getRequest().getColor());
                                else
                                    MagicPotions.getMixPotColor().setFill(mixture.get(1));
                                break;
                            case 3:
                                MagicPotions.getMixPotColor().setFill(Color.BLACK);
                                break;

                            }
                        }
                    });
                } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                        || event.getEventType() == GazeEvent.GAZE_EXITED) {
                    timelineProgressBar.stop();
                    progressIndicator.setOpacity(0);
                    progressIndicator.setProgress(0);
                }
            }
        };
    }

}
