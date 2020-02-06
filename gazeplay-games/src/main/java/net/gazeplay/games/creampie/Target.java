package net.gazeplay.games.creampie;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.Position;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.ArrayList;

/**
 * Created by schwab on 26/12/2016.
 */
@Slf4j
public class Target extends Portrait {

    private final Hand hand;

    final EventHandler<Event> enterEvent;

    private boolean anniOff = true;

    private static final int radius = 100;

    private final RandomPositionGenerator randomPositionGenerator;

    private final Stats stats;

    private final ImageLibrary imageLibrary;

    private final ArrayList<TargetAOI> targetAOIList;

    private static final String SOUNDS_MISSILE = "data/creampie/sounds/missile.mp3";

    public Target(final RandomPositionGenerator randomPositionGenerator, final Hand hand, final Stats stats, final IGameContext gameContext,
                  final ImageLibrary imageLibrary) {

        super(radius, randomPositionGenerator, imageLibrary);
        this.randomPositionGenerator = randomPositionGenerator;
        this.hand = hand;
        this.imageLibrary = imageLibrary;
        this.stats = stats;
        targetAOIList = new ArrayList<>();

        enterEvent = e -> {
            if ((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == MouseEvent.MOUSE_MOVED
                || e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == GazeEvent.GAZE_MOVED)
                && anniOff) {

                anniOff = false;
                stats.incNbGoals();
                enter();
            }
        };

        gameContext.getGazeDeviceManager().addEventFilter(this);

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        this.addEventFilter(GazeEvent.ANY, enterEvent);

        stats.notifyNewRoundReady();
    }

    private void enter() {

        this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        final Animation animation = createAnimation();
        animation.play();

        hand.onTargetHit(this);

        try {
            ForegroundSoundsUtils.playSound(SOUNDS_MISSILE);
        } catch (final Exception e) {
            log.warn("Can't play sound: no associated sound : " + e.toString());
        }

    }

    public ArrayList<TargetAOI> getTargetAOIList() {
        return this.targetAOIList;
    }

    private Animation createAnimation() {
        final Timeline timeline = new Timeline();
        final Timeline timeline2 = new Timeline();

        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(radiusProperty(), getInitialRadius() * 1.6)));
        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(rotateProperty(), getRotate() + (360 * 3))));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(visibleProperty(), false)));

        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(radiusProperty(), radius)));

        final Position newPosition = randomPositionGenerator.newRandomBoundedPosition(getInitialRadius(), 0, 1, 0, 0.8);
        final TargetAOI targetAOI = new TargetAOI(newPosition.getX(), newPosition.getY(), getInitialRadius(),
            System.currentTimeMillis());
        targetAOIList.add(targetAOI);
        timeline2.getKeyFrames()
            .add(new KeyFrame(new Duration(1), new KeyValue(centerXProperty(), newPosition.getX())));
        timeline2.getKeyFrames()
            .add(new KeyFrame(new Duration(1), new KeyValue(centerYProperty(), newPosition.getY())));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),
            new KeyValue(fillProperty(), new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true))));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(rotateProperty(), 0)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(visibleProperty(), true)));

        final SequentialTransition sequence = new SequentialTransition(timeline, timeline2);

        sequence.setOnFinished(actionEvent -> {

            anniOff = true;
            stats.notifyNewRoundReady();
        });

        return sequence;
    }
}
