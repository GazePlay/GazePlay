package net.gazeplay.games.magicpotions;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.io.IOException;

/**
 * @author Johana MARKU
 */

@Slf4j
class Potion extends Parent {

    private final double fixationLength;

    @Getter
    private final Rectangle potion;

    @Getter
    private final Color potionColor;

    @Getter
    private final Image image;

    /**
     * true if the potion has been used/chosen for the mixture
     */
    @Setter
    @Getter
    private boolean chosen;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar; // used to make a selection long = fixation length

    private final MagicPotions gameInstance;

    private final IGameContext gameContext;

    private final MagicPotionsStats stats;
    @Getter
    final EventHandler<Event> enterEvent;

    private Timeline currentTimeline;

    Potion(final double positionX, final double positionY, final double width, final double height, final Image image, final Color color,
           final IGameContext gameContext, final MagicPotionsStats stats, final MagicPotions gameInstance, final int fixationlength) {
        this.potion = new Rectangle((int) positionX, (int) positionY, (int) width, (int) height);
        this.potion.setFill(new ImagePattern(image, 0, 0, 1, 1, true));

        final DropShadow shadow = new DropShadow();
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
        this.stats = stats;
        this.gameInstance = gameInstance;
        this.fixationLength = fixationlength;

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(this.potion);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        currentTimeline = new Timeline();
        this.getChildren().add(this.potion);

        this.progressIndicator = createProgressIndicator(width);
        this.getChildren().add(this.progressIndicator);
    }

    private ProgressIndicator createProgressIndicator(final double width) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(potion.getX());
        indicator.setTranslateY(potion.getY() + width * 0.75);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onMixAchieved() {

        gameInstance.currentRoundDetails.getMixPotColor().setFill(gameInstance.currentRoundDetails.getColorRequest());
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

        currentTimeline.onFinishedProperty().set(event -> gameContext.playWinTransition(0, event1 -> {
            gameInstance.dispose();
            gameContext.clear();
            gameInstance.launch();
            stats.notifyNewRoundReady();
        }));
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

        final Explosion exp = new Explosion(gameContext, gameContext.getGamePanelDimensionProvider().getDimension2D());
        gameContext.getChildren().add(exp);
        gameContext.getChildren().removeAll(gameInstance.currentRoundDetails.getMixPot(), gameInstance.currentRoundDetails.getMixPotColor());

        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(4000), new KeyValue(exp.opacityProperty(), 0)));

        currentTimeline.play();
        currentTimeline.onFinishedProperty().set(event -> {
            gameInstance.dispose();
            gameContext.clear();
            gameInstance.launch();
            try {
                stats.saveStats();
            } catch (final IOException ex) {
                log.info("Io exception");
            }

            stats.notifyNewRoundReady();
        });
    }

    private EventHandler<Event> buildEvent() {
        return event -> {
            if (chosen) {
                return;
            }
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED
                || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                progressIndicator.setOpacity(1);
                progressIndicator.setProgress(0);

                timelineProgressBar = new Timeline();

                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationLength),
                    new KeyValue(progressIndicator.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(event1 -> {
                    chosen = true;
                    gameInstance.currentRoundDetails.getMixture().add(potionColor); // we add the color of the potion to our mixture
                    // change opacity of potion when it has been selected once
                    potion.setOpacity(.3);

                    potion.removeEventFilter(MouseEvent.ANY, enterEvent); // we cannot select anymore this color
                    potion.removeEventFilter(GazeEvent.ANY, enterEvent);

                    if (!gameInstance.currentRoundDetails.getPotionsToMix().contains(potionColor)) {
                        onWrongPotionSelected();
                    } else if (gameInstance.currentRoundDetails.getMixture().containsAll(gameInstance.currentRoundDetails.getPotionsToMix())) {
                        onMixAchieved();
                    } else {
                        stats.incNbGoals();
                    }
                    switch (gameInstance.currentRoundDetails.getMixture().size()) {
                        case 1:
                            gameInstance.currentRoundDetails.getMixPotColor().setFill(gameInstance.currentRoundDetails.getMixture().get(0));
                            break;
                        case 2:
                            if (gameInstance.currentRoundDetails.getPotionsToMix().size() == 2) {
                                gameInstance.currentRoundDetails.getMixPotColor()
                                    .setFill(gameInstance.currentRoundDetails.getRequest().getColor());
                            } else {
                                gameInstance.currentRoundDetails.getMixPotColor().setFill(gameInstance.currentRoundDetails.getMixture().get(1));
                            }
                            break;
                        case 3:
                            gameInstance.currentRoundDetails.getMixPotColor().setFill(Color.BLACK);
                            break;
                        default:
                            throw new IllegalArgumentException("value : " + gameInstance.currentRoundDetails.getMixture().size());
                    }
                });
                timelineProgressBar.play();

            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                timelineProgressBar.stop();
                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }

}
