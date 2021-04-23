package net.gazeplay.games.creampie;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
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

    private final CreamPie gameInstance;

    private boolean animationEnded = true;

    private final int radius;

    private final RandomPositionGenerator randomPositionGenerator;

    private final Stats stats;

    private final ImageLibrary imageLibrary;

    private final ArrayList<TargetAOI> targetAOIList;

    private static final String SOUNDS_MISSILE = "data/creampie/sounds/missile.wav";

    private final IGameContext gameContext;

    public Target(final RandomPositionGenerator randomPositionGenerator, final Hand hand, final Stats stats, final IGameContext gameContext,
                  final ImageLibrary imageLibrary, CreamPie gameInstance, final int radius) {

        super(radius, randomPositionGenerator, imageLibrary);
        this.radius = radius;
        this.randomPositionGenerator = randomPositionGenerator;
        this.hand = hand;
        this.imageLibrary = imageLibrary;
        this.stats = stats;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
        this.targetAOIList = new ArrayList<>();

        enterEvent = e -> {
            if ((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED)
                && animationEnded) {

                animationEnded = false;
                enter();
            }
        };

        gameContext.start();

        gameContext.getGazeDeviceManager().addEventFilter(this);

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        this.addEventFilter(GazeEvent.ANY, enterEvent);

    }

    private void enter() {

        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats,gameInstance);
        this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        final Animation animation = createAnimation();
        animation.play();

        hand.onTargetHit(this);

        gameContext.getSoundManager().add(SOUNDS_MISSILE);

    }

    public ArrayList<TargetAOI> getTargetAOIList() {
        return this.targetAOIList;
    }

    private Animation createAnimation() {
        final Timeline timeline = new Timeline();

        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(radiusProperty(), getInitialRadius() * 1.6)));
        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(rotateProperty(), getRotate() + (360 * 3))));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(visibleProperty(), false)));


        timeline.setOnFinished(actionEvent -> {
            animationEnded = true;
            if(targetAOIList.size()>0){
                targetAOIList.get(targetAOIList.size()-1).setTimeEnded(System.currentTimeMillis());
            }
            newPosition();
        });

        return timeline;
    }

    private void newPosition(){
        final Position newPosition = randomPositionGenerator.newRandomBoundedPosition(getInitialRadius(), 0, 1, 0, 0.8);

        setRadius(radius);
        setCenterX(newPosition.getX());
        setCenterY(newPosition.getY());
        setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
        setRotate(0);
        setVisible(true);

        stats.incrementNumberOfGoalsToReach();

        final TargetAOI targetAOI = new TargetAOI(newPosition.getX(), newPosition.getY(), getInitialRadius(),
            System.currentTimeMillis());
        targetAOIList.add(targetAOI);

    }
}
