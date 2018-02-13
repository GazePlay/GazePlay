package net.gazeplay.games.creampie;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Created by schwab on 26/12/2016.
 */
public class Target extends Portrait {

    private final Hand hand;

    EventHandler<Event> enterEvent;

    private boolean anniOff = true;

    private static int radius = 100;

    private final RandomPositionGenerator randomPositionGenerator;

    private final Stats stats;

    private final Image[] availableImages;

    public Target(RandomPositionGenerator randomPositionGenerator, Hand hand, Stats stats, GameContext gameContext,
            Image[] availableImages) {

        super(radius, randomPositionGenerator, availableImages);

        this.randomPositionGenerator = randomPositionGenerator;
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

        gameContext.getGazeDeviceManager().addEventFilter(this);

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        this.addEventFilter(GazeEvent.ANY, enterEvent);

        stats.start();
    }

    private void enter() {

        this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        hand.onTargetHit(this);

        Animation animation = createAnimation();
        animation.play();
    }

    private Animation createAnimation() {
        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

        timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(2000), new KeyValue(radiusProperty(), getInitialRadius() * 1.6)));
        timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(2000), new KeyValue(rotateProperty(), getRotate() + (360 * 3))));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(visibleProperty(), false)));

        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(radiusProperty(), radius)));

        Position newPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());

        timeline2.getKeyFrames()
                .add(new KeyFrame(new Duration(1), new KeyValue(centerXProperty(), newPosition.getX())));
        timeline2.getKeyFrames()
                .add(new KeyFrame(new Duration(1), new KeyValue(centerYProperty(), newPosition.getY())));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),
                new KeyValue(fillProperty(), new ImagePattern(pickRandomImage(availableImages), 0, 0, 1, 1, true))));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(rotateProperty(), 0)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(visibleProperty(), true)));

        SequentialTransition sequence = new SequentialTransition(timeline, timeline2);

        sequence.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                anniOff = true;
                stats.start();
            }
        });

        return sequence;
    }
}
