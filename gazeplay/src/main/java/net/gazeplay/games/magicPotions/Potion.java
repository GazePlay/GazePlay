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
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

public class Potion extends Parent {

    private static final float zoom_factor = 1.05f;

    private final double fixationLength;

    @Getter // this potion will be Red | Blue | Yellow
    private final Rectangle potion;
    @Getter
    private final Color potionColor;

    @Getter
    private final Image image;

    private final GameContext gameContext;

    private final double initWidth;
    private final double initHeight;
    @Getter
    private final double initX;
    @Getter
    private final double initY;

    // it's true if the potion has been used/chosen for the mixture
    private boolean chosen = false;

    private final ProgressIndicator progressIndicator;
    private Timeline timelineProgressBar;

    final Stats stats;

    final EventHandler<Event> enterEvent;

    private Timeline currentTimeline;

    public Potion(double positionX, double positionY, double width, double height, Image image, Color color , GameContext gameContext, Stats stats, int fixationlength ){
        this.potion = new Rectangle((int)positionX, (int)positionY, (int)width, (int)height);
        this.potion.setFill(new ImagePattern(image,0,0,1,1,true));

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.DARKGRAY);
        shadow.setWidth(10);
        shadow.setHeight(10);
        shadow.setOffsetX(5);
        shadow.setOffsetY(5);
        shadow.setRadius(3);
        this.potion.setEffect(shadow);

        this.image = image;
        this.potionColor = color;

        this.gameContext = gameContext;
        this.stats = stats;
        this.fixationLength = fixationlength;

        this.initWidth = width;
        this.initHeight = height;
        this.initX = positionX;
        this.initY = positionY;

        this.progressIndicator = createProgressIndicator(width, height);
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent(); // create method !

        gameContext.getGazeDeviceManager().addEventFilter(potion);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        currentTimeline = new Timeline();
        this.getChildren().add(this.potion);
    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(potion.getX() + width * 0.05);
        indicator.setTranslateY(potion.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }
    private void onGoodPotionSelected(){

    }
    private void onWrongPotionSelected(){
        currentTimeline.stop();
        currentTimeline = new Timeline();

    }
    private EventHandler<Event> buildEvent(){
        return new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if(chosen)
                    return;
                if(event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED){
                    progressIndicator.setOpacity(1);
                    progressIndicator.setProgress(0);

                    currentTimeline.stop();
                    currentTimeline = new Timeline();

                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(potion.xProperty(), potion.getX() - (initWidth * zoom_factor - initWidth) / 2)));
                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(potion.yProperty(), potion.getY() - (initHeight * zoom_factor - initHeight) / 2)));
                    currentTimeline.getKeyFrames().add(
                            new KeyFrame(new Duration(1), new KeyValue(potion.widthProperty(), initWidth * zoom_factor)));
                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(potion.heightProperty(), initHeight * zoom_factor)));

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationLength),
                            new KeyValue(progressIndicator.progressProperty(), 1)));

                    currentTimeline.play();

                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            chosen = true;

                            // maybe change opacity of potion ..
                            potion.setOpacity(.3);

                            potion.removeEventFilter(MouseEvent.ANY, enterEvent);
                            potion.removeEventFilter(GazeEvent.ANY, enterEvent);

                            // if should select this potion or not

                        }
                    });
                }

            }
        };
    }
}
