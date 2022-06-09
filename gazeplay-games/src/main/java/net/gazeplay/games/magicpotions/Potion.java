package net.gazeplay.games.magicpotions;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.components.ProgressButton;

import java.io.IOException;

/**
 * @author Johana MARKU
 */

@Slf4j
class Potion extends Parent {

    private final double fixationLength;

    @Getter
    private final ImageView potion;

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

    private final ProgressButton progressButton;

    private final MagicPotions gameInstance;

    private final IGameContext gameContext;

    private final MagicPotionsStats stats;
    @Getter
    final EventHandler<Event> enterEvent;

    private Timeline currentTimeline;

    Potion(final double positionX, final double positionY, final double width, final double height, final Image image, final Color color,
           final IGameContext gameContext, final MagicPotionsStats stats, final MagicPotions gameInstance, final int fixationlength) {
        final DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setWidth(10);
        shadow.setHeight(10);
        shadow.setOffsetX(5);
        shadow.setOffsetY(5);
        shadow.setRadius(3);

        this.potion = new ImageView(image);
        this.potion.setFitWidth(width);
        this.potion.setFitHeight(height);
        this.potion.setPreserveRatio(true);
        this.potion.setEffect(shadow);
        this.image = image;
        this.potionColor = color;

        this.chosen = false;
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameInstance = gameInstance;
        this.fixationLength = fixationlength;

        currentTimeline = new Timeline();

        this.progressButton = createProgressIndicator(positionX, positionY);
        this.enterEvent = buildEvent(progressButton);
        progressButton.assignIndicatorUpdatable(enterEvent, gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(this.progressButton);
        this.getChildren().add(this.progressButton);
        progressButton.active();
    }

    private ProgressButton createProgressIndicator(double positionX, double positionY) {
        final ProgressButton indicator = new ProgressButton();
        indicator.setImage(potion);
        indicator.setTranslateX(positionX);
        indicator.setTranslateY(positionY);

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
        stats.incrementNumberOfGoalsReached();
        currentTimeline.stop();
        currentTimeline = new Timeline();

        gameInstance.getPotionBlue().progressButton.disable();
        gameInstance.getPotionRed().progressButton.disable();
        gameInstance.getPotionYellow().progressButton.disable();

        gameContext.updateScore(stats, gameInstance);

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
        progressButton.setStyle(" -fx-progress-color: red;");
        progressButton.setAccessibleText("");

        gameInstance.getPotionBlue().progressButton.disable();
        gameInstance.getPotionRed().progressButton.disable();
        gameInstance.getPotionYellow().progressButton.disable();

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
        });
    }

    private EventHandler<Event> buildEvent(ProgressButton progressButton) {
        return event -> {
            chosen = true;
            gameInstance.currentRoundDetails.getMixture().add(potionColor); // we add the color of the potion to our mixture
            // change opacity of potion when it has been selected once
            progressButton.disable();
            progressButton.setOpacity(.3);

            potion.removeEventFilter(MouseEvent.ANY, enterEvent); // we cannot select anymore this color
            potion.removeEventFilter(GazeEvent.ANY, enterEvent);

            if (!gameInstance.currentRoundDetails.getPotionsToMix().contains(potionColor)) {
                onWrongPotionSelected();
            } else if (gameInstance.currentRoundDetails.getMixture().containsAll(gameInstance.currentRoundDetails.getPotionsToMix())) {
                onMixAchieved();
            } else {
                stats.incrementNumberOfGoalsToReach();
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
                        if ((gameInstance.currentRoundDetails.getMixture().get(0).equals(Color.RED) && gameInstance.currentRoundDetails.getMixture().get(1).equals(Color.YELLOW)) ||
                            (gameInstance.currentRoundDetails.getMixture().get(1).equals(Color.RED) && gameInstance.currentRoundDetails.getMixture().get(0).equals(Color.YELLOW))) {
                            gameInstance.currentRoundDetails.getMixPotColor().setFill(Color.ORANGE);
                        }

                        if ((gameInstance.currentRoundDetails.getMixture().get(0).equals(Color.BLUE) && gameInstance.currentRoundDetails.getMixture().get(1).equals(Color.YELLOW)) ||
                            (gameInstance.currentRoundDetails.getMixture().get(1).equals(Color.BLUE) && gameInstance.currentRoundDetails.getMixture().get(0).equals(Color.YELLOW))) {
                            gameInstance.currentRoundDetails.getMixPotColor().setFill(Color.GREEN);
                        }

                        if ((gameInstance.currentRoundDetails.getMixture().get(0).equals(Color.RED) && gameInstance.currentRoundDetails.getMixture().get(1).equals(Color.BLUE)) ||
                            (gameInstance.currentRoundDetails.getMixture().get(1).equals(Color.RED) && gameInstance.currentRoundDetails.getMixture().get(0).equals(Color.BLUE))) {
                            gameInstance.currentRoundDetails.getMixPotColor().setFill(Color.PURPLE);
                        }
                    }
                    break;
                case 3:
                    gameInstance.currentRoundDetails.getMixPotColor().setFill(Color.BLACK);
                    break;
                default:
                    throw new IllegalArgumentException("value : " + gameInstance.currentRoundDetails.getMixture().size());
            }
        };
    }

}
