package net.gazeplay.games.ninja;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressPortrait;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 26/12/2016.
 */
@Slf4j
public class Target extends ProgressPortrait {

    private static final int nbBall = 20;

    private final IGameContext gameContext;

    private final RandomPositionGenerator randomPositionGenerator;
    private final RandomPositionGenerator randomMiniBallsPositionGenerator;

    private final Stats stats;

    private final List<Portrait> miniBallsPortraits;

    private final ImageLibrary imageLibrary;

    private static final String audioClipResourceLocation = "data/ninja/sounds/2009.wav";

    private final Ninja gameInstance;

    private boolean animationStopped = true;

    private final NinjaGameVariant gameVariant;

    private final ReplayablePseudoRandom randomGen;

    public Animation currentTranslation;

    private final EventHandler<Event> enterEvent;


    public Target(final IGameContext gameContext, final RandomPositionGenerator randomPositionGenerator, final Stats stats,
                  final ImageLibrary imageLibrary, final NinjaGameVariant gameVariant, final Ninja gameInstance, final ReplayablePseudoRandom randomGenerator) {
        super(gameContext.getConfiguration().getElementSize());

        this.gameInstance = gameInstance;
        this.gameContext = gameContext;
        this.randomPositionGenerator = randomPositionGenerator;

        this.randomMiniBallsPositionGenerator = new RandomPositionGenerator(new ReplayablePseudoRandom()) {
            @Override
            public Dimension2D getDimension2D() {
                return gameContext.getGamePanelDimensionProvider().getDimension2D();
            }
        };

        this.stats = stats;
        this.imageLibrary = imageLibrary;
        this.gameVariant = gameVariant;
        this.randomGen = randomGenerator;
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();

        enterEvent = buildEvent();

        this.miniBallsPortraits = generateMiniBallsPortraits(imageLibrary, nbBall);
        gameContext.getChildren().addAll(miniBallsPortraits);

        createTarget();

        move();
        gameContext.start();
    }

    private void createTarget() {

        final Position newPosition = randomPositionGenerator.newRandomBoundedPosition(gameContext.getConfiguration().getElementSize(), 0, 1, 0, 0.8);

        getButton().setRadius(gameContext.getConfiguration().getElementSize());
        setLayoutX(newPosition.getX());
        setLayoutY(newPosition.getY());
        getButton().setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
        setVisible(true);

        assignIndicatorUpdatable(enterEvent,gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(this);
        active();
    }

    private List<Portrait> generateMiniBallsPortraits(final ImageLibrary imageLibrary, final int count) {
        final List<Portrait> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final Portrait miniPortrait = new Portrait(gameContext.getConfiguration().getElementSize() / 2, randomMiniBallsPositionGenerator, imageLibrary);
            miniPortrait.setOpacity(1);
            miniPortrait.setVisible(false);
            result.add(miniPortrait);
        }
        return result;
    }

    private EventHandler<Event> buildEvent() {

        return e -> {

            // if((e.getEventType() == MouseEvent.MOUSE_ENTERED_TARGET ||e.getEventType() == MouseEvent.MOUSE_ENTERED ||
            // e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_ENTERED ||
            // e.getEventType() == GazeEvent.GAZE_MOVED) && anniOff) {

            if (animationStopped) {

                animationStopped = false;
                enter(e);
            }
        };
    }

    private void moveRandom(final int length) {

        final Position currentPosition = new Position((int) getLayoutX(), (int) getLayoutY());

        final Position newPosition = randomPositionGenerator.newRandomPosition(gameContext.getConfiguration().getElementSize());
        resetTargetAtPosition(currentPosition);
        final TranslateTransition translation = new TranslateTransition(
            new Duration(length), this);
        translation.setByX(-this.getLayoutX() + newPosition.getX());
        translation.setByY(-this.getLayoutY() + newPosition.getY());
        translation.setOnFinished(actionEvent -> {
            resetTargetAtPosition(newPosition);

            move();
        });
        translation.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        currentTranslation = translation;
        translation.play();
    }

    private void resetTargetAtPosition(final Position pos) {
        Target.this.setScaleX(1);
        Target.this.setScaleY(1);
        Target.this.setScaleZ(1);

        Target.this.setLayoutX(pos.getX());
        Target.this.setLayoutY(pos.getY());

        Target.this.setTranslateX(0);
        Target.this.setTranslateY(0);
        Target.this.setTranslateZ(0);
    }

    private void createBackAndForthTranlations(final Position pos1, final Position pos2, final int length) {

        final Timeline translation1 = new Timeline(new KeyFrame(new Duration(length),
            new KeyValue(this.layoutXProperty(), pos1.getX()), new KeyValue(this.layoutYProperty(), pos1.getY())));
        translation1.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        final Timeline translation2 = new Timeline(new KeyFrame(new Duration(length),
            new KeyValue(this.layoutXProperty(), pos2.getX()), new KeyValue(this.layoutYProperty(), pos2.getY())));
        translation2.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        translation1.setOnFinished(actionEvent -> {
            resetTargetAtPosition(pos1);

            currentTranslation = translation2;
            translation2.play();
        });

        translation2.setOnFinished(actionEvent -> {
            resetTargetAtPosition(pos2);

            currentTranslation = translation1;
            translation1.play();
        });

        setLayoutX(pos2.getX());
        setLayoutY(pos2.getY());
        currentTranslation = translation1;
        translation1.playFrom(new Duration(length).multiply(randomGen.nextDouble()));
    }

    private void move() {
        final int length = randomGen.nextInt(2000) + 1000;// between 1 and 3 seconds

        final Dimension2D dimension2D = randomPositionGenerator.getDimension2D();

        switch (gameVariant) {
            case RANDOM: // random
                moveRandom(length);
                break;
            case VERTICAL: // vertical
                createBackAndForthTranlations(new Position(getLayoutX(), gameContext.getConfiguration().getElementSize()),
                    new Position(getLayoutX(), dimension2D.getHeight() - gameContext.getConfiguration().getElementSize()), length * 2);
                break;
            case HORIZONTAL: // horizontal
                createBackAndForthTranlations(new Position(gameContext.getConfiguration().getElementSize(), getLayoutY()),
                    new Position(dimension2D.getWidth() - gameContext.getConfiguration().getElementSize(), getLayoutY()), length * 2);
                break;
            case DIAGONAL_UPPER_LEFT_TO_LOWER_RIGHT: // Diagonal \
                createBackAndForthTranlations(new Position(gameContext.getConfiguration().getElementSize(), gameContext.getConfiguration().getElementSize()),
                    new Position(dimension2D.getWidth() - gameContext.getConfiguration().getElementSize(),
                        dimension2D.getHeight() - gameContext.getConfiguration().getElementSize()),
                    length * 2);
                break;
            case DIAGONAL_UPPER_RIGHT_TO_LOWER_LEFT: // Diagonal /
                createBackAndForthTranlations(new Position(dimension2D.getWidth() - gameContext.getConfiguration().getElementSize(), gameContext.getConfiguration().getElementSize()),
                    new Position(0, dimension2D.getHeight() - gameContext.getConfiguration().getElementSize()), length * 2);
                break;
        }

    }

    public Position getPointerPosition(final Event e) {
        double mousePositionX = 0;
        double mousePositionY = 0;
        if (e instanceof MouseEvent) {
            final MouseEvent mouseEvent = (MouseEvent) e;
            mousePositionX = mouseEvent.getX();
            mousePositionY = mouseEvent.getY();
        }
        if (e instanceof GazeEvent) {
            final GazeEvent gazeEvent = (GazeEvent) e;
            mousePositionX = gazeEvent.getX();
            mousePositionY = gazeEvent.getY();
        }
        return new Position((int) mousePositionX, (int) mousePositionY);
    }

    private void enter(final Event e) {

        stats.incrementNumberOfGoalsReached();

        gameContext.updateScore(stats, gameInstance);

        final Animation runningTranslation = currentTranslation;
        if (runningTranslation != null) {
            runningTranslation.stop();
        }

        // this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        final Transition transition1 = createTransition1();

        final Transition transition2 = createTransition2();

        final Timeline childrenTimelineStart = new Timeline();
        final Timeline childrenTimelineEnd = new Timeline();

        final Position currentPositionWithTranslation = getCurrentCenterPositionWithTranslation();

        final Position pointerPosition = getPointerPosition(e);
        log.debug("pointerPosition = {}, currentPositionWithTranslation = {}", pointerPosition,
            currentPositionWithTranslation);

        for (final Portrait childMiniBall : miniBallsPortraits) {
            childMiniBall.setPosition(currentPositionWithTranslation);
            childMiniBall.setOpacity(1);
            childMiniBall.setVisible(true);

            final Position childBallTargetPosition = randomMiniBallsPositionGenerator.newRandomPosition(gameContext.getConfiguration().getElementSize());

            childrenTimelineEnd.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(childMiniBall.centerXProperty(),
                    childBallTargetPosition.getX(), Interpolator.EASE_OUT)));

            childrenTimelineEnd.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(childMiniBall.centerYProperty(),
                    childBallTargetPosition.getY(), Interpolator.EASE_OUT)));

            childrenTimelineEnd.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(childMiniBall.opacityProperty(), 0)));
        }

        final Position newPosition = randomPositionGenerator.newRandomPosition(gameContext.getConfiguration().getElementSize());

        final Timeline selfTimeLine = new Timeline();

        selfTimeLine.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(getButton().radiusProperty(), gameContext.getConfiguration().getElementSize())));

        selfTimeLine.getKeyFrames()
            .add(new KeyFrame(new Duration(1000), new KeyValue(layoutXProperty(), newPosition.getX())));

        selfTimeLine.getKeyFrames()
            .add(new KeyFrame(new Duration(1000), new KeyValue(layoutYProperty(), newPosition.getY())));

        selfTimeLine.getKeyFrames().add(new KeyFrame(new Duration(1000),
            new KeyValue(getButton().fillProperty(), new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true))));

        final Transition transition4 = createTransition4();

        final SequentialTransition sequence = new SequentialTransition(transition1, transition2, childrenTimelineStart,
            childrenTimelineEnd, selfTimeLine, transition4);

        sequence.setOnFinished(actionEvent -> {
            animationStopped = true;
            stats.incrementNumberOfGoalsToReach();
            move();
        });

        sequence.play();
        gameContext.getSoundManager().add(audioClipResourceLocation);
    }

    private Transition createTransition1() {
        final FadeTransition fadeTransition = new FadeTransition(new Duration(1), this);
        fadeTransition.setToValue(0.5);

        final Timeline timeline1 = new Timeline();
        timeline1.getKeyFrames().add(new KeyFrame(new Duration(100), new KeyValue(getButton().radiusProperty(), gameContext.getConfiguration().getElementSize()/2)));
        return new ParallelTransition(fadeTransition, timeline1);
    }

    private Transition createTransition2() {
        final FadeTransition fadeTransition = new FadeTransition(new Duration(1), this);
        fadeTransition.setToValue(0);
        return fadeTransition;
    }

    private Transition createTransition4() {
        final FadeTransition fadeTransition = new FadeTransition(new Duration(1), this);
        fadeTransition.setToValue(1);
        return fadeTransition;
    }
}
