package net.gazeplay.games.creampie;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import net.gazeplay.commons.gaze.GazeEvent;
import net.gazeplay.commons.gaze.GazeUtils;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.stats.ShootGamesStats;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Created by schwab on 26/12/2016.
 */
public class Target extends Portrait {

    private Hand hand;

    EventHandler<Event> enterEvent;

    private boolean anniOff = true;

    private static int radius = 100;

    private final Scene scene;

    private final Stats stats;

    private final Image[] availableImages;

    public Target(Scene scene, Hand hand, ShootGamesStats stats, Image[] availableImages) {

        super(radius, scene, availableImages);

        this.scene = scene;
        this.hand = hand;
        this.availableImages = availableImages;
        this.stats = stats;

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if ((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == MouseEvent.MOUSE_MOVED
                        || e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == GazeEvent.GAZE_MOVED)
                        && anniOff) {

                    anniOff = false;
                    stats.incNbGoals();
                    enter();
                }
            }
        };

        GazeUtils.addEventFilter(this);

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        this.addEventFilter(GazeEvent.ANY, enterEvent);

        stats.start();
    }

    private void enter() {

        this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        hand.fireEvent(new net.gazeplay.games.creampie.event.TouchEvent(getCenterX(), getCenterY()));

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

        timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(2000), new KeyValue(radiusProperty(), getInitialRadius() * 1.6)));
        timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(2000), new KeyValue(rotateProperty(), getRotate() + (360 * 3))));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(visibleProperty(), false)));

        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(radiusProperty(), radius)));

        Position newPosition = randomPositionGenerator.newRandomPosition(getInitialRadius(), scene);

        timeline2.getKeyFrames()
                .add(new KeyFrame(new Duration(1), new KeyValue(centerXProperty(), newPosition.getX())));
        timeline2.getKeyFrames()
                .add(new KeyFrame(new Duration(1), new KeyValue(centerYProperty(), newPosition.getY())));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),
                new KeyValue(fillProperty(), new ImagePattern(pickRandomImage(availableImages), 0, 0, 1, 1, true))));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(rotateProperty(), 0)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(visibleProperty(), true)));

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
