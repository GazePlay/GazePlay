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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressPortrait;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.ArrayList;

/**
 * Created by schwab on 26/12/2016.
 */
@Slf4j
public class Target extends ProgressPortrait {
    private final Hand hand;

    final EventHandler<Event> enterEvent;

    private final CreamPie gameInstance;

    private boolean animationEnded = true;

    private final RandomPositionGenerator randomPositionGenerator;

    private final Stats stats;


    private final ImageLibrary imageLibrary;

    private final ArrayList<TargetAOI> targetAOIList;

    private static final String SOUNDS_MISSILE = "data/creampie/sounds/missile.mp3";

    private final IGameContext gameContext;

    private boolean onUse = false;

    @Getter
    private double centerX;

    @Getter
    private double centerY;

    public Target(final RandomPositionGenerator randomPositionGenerator, final Hand hand, final Stats stats, final IGameContext gameContext,
                  final ImageLibrary imageLibrary, CreamPie gameInstance) {
        super(gameContext.getConfiguration().getElementSize());
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
            if (!onUse) {
                animationEnded = false;
                onUse = true;
                disableProgressIndicator();
                enter();
            }
        };

        createTarget();

        gameContext.start();
    }

    private void createTarget() {
        final Position newPosition = randomPositionGenerator.newRandomBoundedPositionCreamPie(gameContext.getConfiguration().getElementSize(), 0, 1, 0, 0.8);
        this.centerX = newPosition.getX();
        this.centerY = newPosition.getY();

        setLayoutX(newPosition.getX() - gameContext.getConfiguration().getElementSize());
        setLayoutY(newPosition.getY() - gameContext.getConfiguration().getElementSize());
        getButton().setRadius(gameContext.getConfiguration().getElementSize());
        getButton().setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
        setRotate(0);
        setVisible(true);

        assignIndicatorUpdatable(enterEvent, gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(this);
        active();
    }

    private void enter() {
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, gameInstance);
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
            .add(new KeyFrame(new Duration(2000), new KeyValue(getButton().radiusProperty(), gameContext.getConfiguration().getElementSize() * 1.6)));
        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(layoutXProperty(), centerX - gameContext.getConfiguration().getElementSize() * 1.6)));
        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(layoutYProperty(), centerY - gameContext.getConfiguration().getElementSize() * 1.6)));
        timeline.getKeyFrames()
            .add(new KeyFrame(new Duration(2000), new KeyValue(getButton().rotateProperty(), getButton().getRotate() + (360 * 3))));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(visibleProperty(), false)));


        timeline.setOnFinished(actionEvent -> {
            animationEnded = true;
            onUse = false;
            if (targetAOIList.size() > 0) {
                targetAOIList.get(targetAOIList.size() - 1).setTimeEnded(System.currentTimeMillis());
            }
            newPosition();
        });

        return timeline;
    }

    private void newPosition() {
        final Position newPosition = randomPositionGenerator.newRandomBoundedPositionCreamPie(gameContext.getConfiguration().getElementSize(), 0, 1, 0, 0.8);
        this.centerX = newPosition.getX();
        this.centerY = newPosition.getY();

        setLayoutX(newPosition.getX() - gameContext.getConfiguration().getElementSize());
        setLayoutY(newPosition.getY() - gameContext.getConfiguration().getElementSize());
        getButton().setRadius(gameContext.getConfiguration().getElementSize());
        getButton().setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
        setVisible(true);
        active();

        stats.incrementNumberOfGoalsToReach();

        final TargetAOI targetAOI = new TargetAOI(newPosition.getX(), newPosition.getY(), gameContext.getConfiguration().getElementSize(),
            System.currentTimeMillis());
        targetAOIList.add(targetAOI);

    }
}
