package creampie;

import gaze.GazeEvent;
import gaze.GazeUtils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import utils.games.Portrait;
import utils.games.Stats;

/**
 * Created by schwab on 26/12/2016.
 */
public class Target extends Portrait {

    private Hand hand;

    EventHandler<Event> enterEvent;

    private boolean anniOff = true;

    private static int radius = 100;

    private Stats stats;

    public Target(Hand hand, Stats stats) {

        super(radius);

        this.hand = hand;

        this.stats = stats;

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == GazeEvent.GAZE_MOVED)  && anniOff) {

                    anniOff = false;
                    stats.incNbShoot();
                    enter();
                }
            }
        };

        GazeUtils.addEventFilter(this);

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        this.addEventFilter(GazeEvent.ANY, enterEvent);

        stats.start();
    }

    private void enter(){

        this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        hand.fireEvent(new creampie.event.TouchEvent(getCenterX(), getCenterY()));

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(radiusProperty(), getRadius()*1.6)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(rotateProperty(), getRotate()+(360*3))));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(visibleProperty(),false)));

        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(radiusProperty(), radius)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(centerXProperty(), newX())));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(centerYProperty(), newY())));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fillProperty(), new ImagePattern(newPhoto(), 0, 0, 1,1, true))));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(rotateProperty(), 0)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000),new KeyValue(visibleProperty(),true)));

        SequentialTransition sequence = new SequentialTransition(timeline, timeline2);
        sequence.play();
        sequence.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                anniOff = true;
                stats.start();
            }
        });
    }
}
