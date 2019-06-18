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
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.LinkedList;

public class Potion extends Parent {

    private final double fixationLength;

    @Getter
    private final Rectangle potion;

    @Getter
    private Color potionColor;

    @Getter
    private final Image image;

    @Getter
    private final LinkedList<Color> toMix;

    private final GameContext gameContext;

    private Explosion explosion;

    // it's true if the potion has been used/chosen for the mixture
    @Getter
    @Setter
    private boolean chosen = false;

    private final ProgressIndicator progressIndicator;
    private Timeline timelineProgressBar;

    final Stats stats;

    final EventHandler<Event> enterEvent;

    private Timeline currentTimeline;

    public Potion(double positionX, double positionY, double width, double height, Image image, Color color,
            GameContext gameContext, Stats stats, int fixationlength, LinkedList<Color> toMix) {
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
        this.toMix = toMix;

        this.gameContext = gameContext;
        this.stats = stats;
        this.fixationLength = fixationlength;

        this.enterEvent = buildEvent(); // create method !

        gameContext.getGazeDeviceManager().addEventFilter(potion);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        currentTimeline = new Timeline();
        this.getChildren().add(this.potion);

        this.progressIndicator = createProgressIndicator(width, width);
        this.getChildren().add(this.progressIndicator);
    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(potion.getX());
        indicator.setTranslateY(potion.getY() + height * 0.75);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onGoodPotionSelected() {

    }

    private void onWrongPotionSelected() {
        // currentTimeline.stop();
        // currentTimeline = new Timeline();

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

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {

                            chosen = true;

                            // change opacity of potion when it has been selected once
                            potion.setOpacity(.3);

                            // if should select this potion or not
                            if (!toMix.contains(potionColor)) {
                                // play explosion animation !!!
                                 //explosion = new Explosion();
                                gameContext.playExplosion( e -> {
                                    gameContext.clear();
                                    //Blocs.this.launch();
                                    //gameContext.onGameStarted();
                                });

                            } else {
                                if (MagicPotions.getColorsMixedCounter() == 0) {
                                    MagicPotions.setColorsMixedCounter(MagicPotions.getColorsMixedCounter() + 1);
                                    MagicPotions.getMixPotColor().setFill(potionColor);
                                } else if (MagicPotions.getColorsMixedCounter() == 1) {
                                    if (toMix.size() == 2) {
                                        MagicPotions.getMixPotColor().setFill(MagicPotions.getColorRequest());
                                        MagicPotions.setColorsMixedCounter(MagicPotions.getColorsMixedCounter() + 1);
                                    }
                                }
                                if (toMix.size() == MagicPotions.getColorsMixedCounter()) {
                                    MagicPotions.setPotionMixAchieved(true);
                                    gameContext.playWinTransition(350, null);
                                }

                                potion.removeEventFilter(MouseEvent.ANY, enterEvent);
                                potion.removeEventFilter(GazeEvent.ANY, enterEvent);
                            }

                        }
                    });
                }

            }
        };
    }

    public boolean isChosen() {
        return this.chosen;
    }
}
