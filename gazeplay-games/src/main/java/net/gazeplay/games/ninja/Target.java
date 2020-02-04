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
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.Position;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by schwab on 26/12/2016.
 */
@Slf4j
public class Target extends Portrait {

    private static final int radius = 100;

    private static final int ballRadius = 50;

    private static final int nbBall = 20;

    private final IGameContext gameContext;

    private final RandomPositionGenerator randomPositionGenerator;

    private final Stats stats;

    private final List<Portrait> miniBallsPortraits;

    private final ImageLibrary imageLibrary;

    private static final String audioClipResourceLocation = "data/ninja/sounds/2009.wav";

    private boolean animationStopped = true;

    private final NinjaGameVariant gameVariant;

    private final Random randomGen;

    public Animation currentTranslation;

    public Target(final IGameContext gameContext, final RandomPositionGenerator randomPositionGenerator, final Stats stats,
                  final ImageLibrary imageLibrary, final NinjaGameVariant gameVariant) {
        super(radius, randomPositionGenerator, imageLibrary);

        this.gameContext = gameContext;
        this.randomPositionGenerator = randomPositionGenerator;
        this.stats = stats;
        this.imageLibrary = imageLibrary;
        this.gameVariant = gameVariant;
        this.randomGen = new Random();

        this.miniBallsPortraits = generateMiniBallsPortraits(randomPositionGenerator, imageLibrary, nbBall);
        gameContext.getChildren().addAll(miniBallsPortraits);

        final EventHandler<Event> enterEvent = buildEvent();

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(this);
        this.addEventHandler(GazeEvent.ANY, enterEvent);

        move();

        stats.notifyNewRoundReady();
    }

    private List<Portrait> generateMiniBallsPortraits(final RandomPositionGenerator randomPositionGenerator,
                                                      final ImageLibrary imageLibrary, final int count) {
        final List<Portrait> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final Portrait miniPortrait = new Portrait(ballRadius, randomPositionGenerator, imageLibrary);
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

            if (animationStopped
                && (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED)) {

                animationStopped = false;
                enter(e);
            }
        };
    }

    private void moveRandom(final int length) {

        final Position currentPosition = new Position((int) getCenterX(), (int) getCenterY());

        final Position newPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());
        log.debug("currentPosition = {}, newPosition = {}, length = {}", currentPosition, newPosition, length);

        final TranslateTransition translation = new TranslateTransition(
            new Duration(length), this);
        translation.setByX(-this.getCenterX() + newPosition.getX());
        translation.setByY(-this.getCenterY() + newPosition.getY());
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

        Target.this.setPosition(pos);

        Target.this.setTranslateX(0);
        Target.this.setTranslateY(0);
        Target.this.setTranslateZ(0);
    }

    private void createBackAndForthTranlations(final Position pos1, final Position pos2, final int length) {

        final Timeline translation1 = new Timeline(new KeyFrame(new Duration(length),
            new KeyValue(this.centerXProperty(), pos1.getX()), new KeyValue(this.centerYProperty(), pos1.getY())));
        translation1.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        final Timeline translation2 = new Timeline(new KeyFrame(new Duration(length),
            new KeyValue(this.centerXProperty(), pos2.getX()), new KeyValue(this.centerYProperty(), pos2.getY())));
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

        setPosition(pos2);
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
                createBackAndForthTranlations(new Position(getCenterX(), getInitialRadius()),
                    new Position(getCenterX(), dimension2D.getHeight() - getInitialRadius()), length * 2);
                break;
            case HORIZONTAL: // horizontal
                createBackAndForthTranlations(new Position(getInitialRadius(), getCenterY()),
                    new Position(dimension2D.getWidth() - getInitialRadius(), getCenterY()), length * 2);
                break;
            case DIAGONAL_UPPER_LEFT_TO_LOWER_RIGHT: // Diagonal \
                createBackAndForthTranlations(new Position(getInitialRadius(), getInitialRadius()),
                    new Position(dimension2D.getWidth() - getInitialRadius(),
                        dimension2D.getHeight() - getInitialRadius()),
                    length * 2);
                break;
            case DIAGONAL_UPPER_RIGHT_TO_LOWER_LEFT: // Diagonal /
                createBackAndForthTranlations(new Position(dimension2D.getWidth() - getInitialRadius(), getInitialRadius()),
                    new Position(0, dimension2D.getHeight() - getInitialRadius()), length * 2);
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

        stats.incNbGoals();

        final Animation runningTranslation = currentTranslation;
        if (runningTranslation != null) {
            runningTranslation.stop();
        }

        // this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        final Transition transition1 = createTransition1();

        final Transition transition2 = createTransition2();

        final Timeline childrenTimelineStart = new Timeline();
        final Timeline childrenTimelineEnd = new Timeline();

        final Position currentPositionWithTranslation = getCurrentPositionWithTranslation();

        final Position pointerPosition = getPointerPosition(e);
        log.debug("pointerPosition = {}, currentPositionWithTranslation = {}", pointerPosition,
            currentPositionWithTranslation);

        for (final Portrait childMiniBall : miniBallsPortraits) {
            childMiniBall.setPosition(currentPositionWithTranslation);
            childMiniBall.setOpacity(1);
            childMiniBall.setVisible(true);

            final Position childBallTargetPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());

            childrenTimelineEnd.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(childMiniBall.centerXProperty(),
                    childBallTargetPosition.getX(), Interpolator.EASE_OUT)));

            childrenTimelineEnd.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(childMiniBall.centerYProperty(),
                    childBallTargetPosition.getY(), Interpolator.EASE_OUT)));

            childrenTimelineEnd.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(childMiniBall.opacityProperty(), 0)));
        }

        final Position newPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());

        final Timeline selfTimeLine = new Timeline();

        selfTimeLine.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(radiusProperty(), radius)));

        selfTimeLine.getKeyFrames()
            .add(new KeyFrame(new Duration(1000), new KeyValue(centerXProperty(), newPosition.getX())));

        selfTimeLine.getKeyFrames()
            .add(new KeyFrame(new Duration(1000), new KeyValue(centerYProperty(), newPosition.getY())));

        selfTimeLine.getKeyFrames().add(new KeyFrame(new Duration(1000),
            new KeyValue(fillProperty(), new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true))));

        final Transition transition4 = createTransition4();

        final SequentialTransition sequence = new SequentialTransition(transition1, transition2, childrenTimelineStart,
            childrenTimelineEnd, selfTimeLine, transition4);

        sequence.setOnFinished(actionEvent -> {
            animationStopped = true;
            stats.notifyNewRoundReady();
            move();
        });

        sequence.play();

        try {
            ForegroundSoundsUtils.playSound(audioClipResourceLocation);
        } catch (final Exception exp) {
            log.warn("file doesn't exist : {}", audioClipResourceLocation);
            log.warn(exp.getMessage());
        }
    }

    private Transition createTransition1() {
        final FadeTransition fadeTransition = new FadeTransition(new Duration(1), this);
        fadeTransition.setToValue(0.5);

        final Timeline timeline1 = new Timeline();
        timeline1.getKeyFrames().add(new KeyFrame(new Duration(100), new KeyValue(radiusProperty(), ballRadius)));
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
