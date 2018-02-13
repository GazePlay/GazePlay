package net.gazeplay.games.cups;

import java.awt.Point;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.magiccards.MagicCards;

@Slf4j
public class Cup extends Parent {

    @Getter
    private final Rectangle item;
    private boolean hasBall;
    @Getter @Setter
    private Ball ball;
    
    @Getter
    PositionCup positionCup;
    
    @Getter
    private final double widthItem;
    @Getter
    private final double heightItem;
    @Getter @Setter
    private ProgressIndicator progressIndicator;
    
    /* Taken from Magic Cards */ 
    private final GameContext gameContext;

    private final CupsAndBalls gameInstance;

    /**
     * true if the cup has been revealed
     */
    private boolean revealed = false;

    private Timeline timelineProgressBar;
    final Stats stats;

    final EventHandler<Event> enterEvent;
    @Getter @Setter
    boolean winner;

    public Cup(Rectangle item, PositionCup positionCup, GameContext gameContext, Stats stats, CupsAndBalls gameInstance) {
        this.item = item;
        this.widthItem = item.getWidth();
        this.heightItem = item.getHeight();
        this.positionCup = positionCup;

        this.gameContext = gameContext;

        this.stats = stats;

        this.gameInstance = gameInstance;
        
        this.progressIndicator = createProgressIndicator();
        
        this.enterEvent = buildEvent();
        
        gameContext.getGazeDeviceManager().addEventFilter(item);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);
    }

    public boolean containsBall() {
        return hasBall;
    }

    public void giveBall(boolean hasBall) {
        this.hasBall = hasBall;
    }

    public void updatePosition(int newCellX, int newCellY) {
        Point newPosCup = positionCup.calculateXY(newCellX, newCellY);
        item.setX(newPosCup.getX());
        item.setY(newPosCup.getY());     
    }

    private ProgressIndicator createProgressIndicator() {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(item.getX() + item.getWidth() * 0.05);
        indicator.setTranslateY(item.getY() + item.getHeight() * 0.2);
        indicator.setMinWidth(item.getWidth() * 0.9);
        indicator.setMinHeight(item.getHeight() * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onCorrectCardSelected() {
        stats.incNbGoals();

        progressIndicator.setOpacity(0);

        gameInstance.removeAllIncorrectCups();

        TranslateTransition revealBallTransition = new TranslateTransition(Duration.millis(1000/*3000*/), item);
        revealBallTransition.setByY(-ball.getRadius()*8);

        /*revealBallTransition.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

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
        });*/

        revealBallTransition.play();
    }

    private void onWrongCardSelected() {

        TranslateTransition revealBallTransition = new TranslateTransition(Duration.millis(1000/*3000*/), item);
        revealBallTransition.setByY(-ball.getRadius()*8);
        
        revealBallTransition.play();

        progressIndicator.setOpacity(0);
    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (revealed)
                    return;

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    progressIndicator.setOpacity(1);
                    progressIndicator.setProgress(0);

                    Timeline timelineChoice = new Timeline();

                    /* DO THIS PART */
                    /*timelineChoice.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(item.xProperty(), item.getX() - (initWidth * zoom_factor - initWidth) / 2)));
                    timelineChoice.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(item.yProperty(), item.getY() - (initHeight * zoom_factor - initHeight) / 2)));
                    timelineChoice.getKeyFrames().add(
                            new KeyFrame(new Duration(1), new KeyValue(item.widthProperty(), initWidth * zoom_factor)));
                    timelineChoice.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(item.heightProperty(), initHeight * zoom_factor)));

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                            new KeyValue(progressIndicator.progressProperty(), 1)));

                    timelineChoice.play();

                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {

                            revealed = true;

                            item.setFill(new ImagePattern(image, 0, 0, 1, 1, true));

                            item.removeEventFilter(MouseEvent.ANY, enterEvent);
                            item.removeEventFilter(GazeEvent.ANY, enterEvent);

                            if (winner) {
                                onCorrectCardSelected();
                            } else {// bad card
                                onWrongCardSelected();
                            }
                        }
                    });
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    Timeline timeline = new Timeline();

                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(item.xProperty(), item.getX() + (initWidth * zoom_factor - initWidth) / 2)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(item.yProperty(), item.getY() + (initHeight * zoom_factor - initHeight) / 2)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(item.widthProperty(), initWidth)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(item.heightProperty(), initHeight)));

                    timeline.play();

                    timelineProgressBar.stop();

                    progressIndicator.setOpacity(0);
                    progressIndicator.setProgress(0);
                }*/
            }
        }
    };
}
}
