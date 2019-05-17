/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.slidingpuzzle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.math101.Math101;

/**
 *
 * @author Peter Bardawil
 */
@Slf4j
public class slidingpuzzlecard extends Parent {

    public slidingpuzzlecard(int id, double positionX, double positionY, double width, double height, String fileName,
            double fixationlength, GameContext gameContext, slidingpuzzle gameInstance, Stats stats,double kingPosX,double kingPosY) {
        this.fixationlength = fixationlength;
        this.CardId = id;
        this.card = new Rectangle(positionX, positionY, width, height);
        this.card.setFill(new ImagePattern(new Image(fileName), 0, 0, 1, 1, true));
        this.gameContext = gameContext;
        this.initWidth = width;
        this.initHeight = height;
        this.initX = positionX;
        this.initY = positionY;
        this.gameInstance = gameInstance;
        this.stats = stats;
        this.progressIndicator = createProgressIndicator(width, height);
        this.getChildren().add(this.progressIndicator);
        this.getChildren().add(this.card);
        this.kingPosX = kingPosX;
        this.kingPosY = kingPosY;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
         this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(card);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        // Prevent null pointer exception
        currentTimeline = new Timeline();
    }

    private static final float zoom_factor = 1.05f;

    private final double fixationlength;

    private final Rectangle card;

    private final int CardId;
    
    private static javafx.geometry.Dimension2D dimension2D;

    private final GameContext gameContext;

    private final double initWidth;
    private final double initHeight;

    private final double initX;
    private final double initY;

    private double kingPosX , kingPosY;
    private final slidingpuzzle gameInstance;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;

    final Stats stats;

    final EventHandler<Event> enterEvent;

    /**
     * Use a comma Timeline object so we can stop the current animation to prevent overlapses.
     */
    private Timeline currentTimeline;

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX() + width * 0.05);
        indicator.setTranslateY(card.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }
    private static double computeCardBoxHeight(int nbLines) {
        return dimension2D.getHeight() / nbLines;
    }

    private static double computeCardBoxWidth(int nbColumns) {
        
        return dimension2D.getWidth() / nbColumns;
    }
    
    private Boolean checkIfNeighbor(){
        log.info("index = " + this.CardId);
        log.info("initX = " + this.initX);
        log.info("initY = " + this.initY);
        log.info("KingPosX = " + this.kingPosX);
        log.info("KingPosY = " + this.kingPosY);
        log.info("BoxWidth = " + computeCardBoxWidth(3));
        log.info("BoxHeight = " + computeCardBoxHeight(3));
    if (this.initX==kingPosX && ((this.initY==kingPosY + 200)  || (this.initY==kingPosY - 200)))
        return true;
    else if (this.initY==kingPosY && ((this.initX==kingPosX + 200)  || (this.initX==kingPosX - 200)))
        return true;
    
    else return false;
    }
    
    private void isMyNeighborEvent() {
        
    progressIndicator.setOpacity(0);
    currentTimeline.stop();
    currentTimeline = new Timeline();
    KeyValue xValue  = new KeyValue(card.xProperty(), kingPosX); 
    KeyValue yValue  = new KeyValue(card.yProperty(), kingPosY);
    KeyFrame keyFrame  = new KeyFrame(Duration.millis(100), xValue, yValue);
    currentTimeline.getKeyFrames().add(keyFrame);
    currentTimeline.play();
        
}
    private EventHandler<Event> buildEvent() {

        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {


                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    progressIndicator.setOpacity(1);
                    progressIndicator.setProgress(0);

                    currentTimeline.stop();
                    currentTimeline = new Timeline();
                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.xProperty(), card.getX() - (initWidth * zoom_factor - initWidth) / 2)));
                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.yProperty(), card.getY() - (initHeight * zoom_factor - initHeight) / 2)));
                    currentTimeline.getKeyFrames().add(
                            new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth * zoom_factor)));
                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.heightProperty(), initHeight * zoom_factor)));

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                            new KeyValue(progressIndicator.progressProperty(), 1)));

                    currentTimeline.play();

                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {

                            log.info("Check if Neighbor = " + checkIfNeighbor().toString());
                            if (checkIfNeighbor()) {
                               
                                isMyNeighborEvent();
                            }
                        }
                    });
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    currentTimeline.stop();
                    currentTimeline = new Timeline();

                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.xProperty(), card.getX() + (initWidth * zoom_factor - initWidth) / 2)));
                    currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.yProperty(), card.getY() + (initHeight * zoom_factor - initHeight) / 2)));
                    currentTimeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth)));
                    currentTimeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(card.heightProperty(), initHeight)));

                    // Be sure that the card is properly positionned at the end
                    currentTimeline.setOnFinished((event) -> {
                        card.setX(initX);
                        card.setY(initY);
                    });

                    currentTimeline.play();

                    timelineProgressBar.stop();

                    progressIndicator.setOpacity(0);
                    progressIndicator.setProgress(0);
                }
            }
        };
    }
     
}
