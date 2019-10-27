package net.gazeplay.games.ninja;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.Utils;
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

    private final RandomPositionGenerator randomPositionGenerator;

    private final Stats stats;

    private final List<Portrait> miniBallsPortraits;

    private final ImageLibrary imageLibrary;

    private final EventHandler<Event> enterEvent;

    private static final String audioClipResourceLocation = "data/ninja/sounds/2009.wav";

    private boolean animationStopped = true;

    private final int gameVariant;

    private Random randomGen;

    public Animation currentTranslation;

    public Target(IGameContext gameContext, RandomPositionGenerator randomPositionGenerator, Stats stats,
            ImageLibrary imageLibrary, int gameVariant) {
        super(radius, randomPositionGenerator, imageLibrary);

        this.randomPositionGenerator = randomPositionGenerator;
        this.stats = stats;
        this.imageLibrary = imageLibrary;
        this.gameVariant = gameVariant;
        this.randomGen = new Random();

        this.miniBallsPortraits = generateMiniBallsPortraits(randomPositionGenerator, imageLibrary, nbBall);
        gameContext.getChildren().addAll(miniBallsPortraits);

        enterEvent = buildEvent();

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(this);
        this.addEventHandler(GazeEvent.ANY, enterEvent);

        move();

        stats.notifyNewRoundReady();
    }

    private List<Portrait> generateMiniBallsPortraits(RandomPositionGenerator randomPositionGenerator,
            ImageLibrary imageLibrary, int count) {
        List<Portrait> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Portrait miniPortrait = new Portrait(ballRadius, randomPositionGenerator, imageLibrary);
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

    private void moveRandom(int length) {

        final Position currentPosition = new Position((int) getCenterX(), (int) getCenterY());

        final Position newPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());
        log.debug("currentPosition = {}, newPosition = {}, length = {}", currentPosition, newPosition, length);

        TranslateTransition translation = new TranslateTransition(
                new Duration(Configuration.getInstance().getSpeedEffects() * length), this);
        translation.setByX(-this.getCenterX() + newPosition.getX());
        translation.setByY(-this.getCenterY() + newPosition.getY());
        translation.setOnFinished(actionEvent -> {
            resetTargetAtPosition(newPosition);

            move();
        });

        currentTranslation = translation;
        translation.play();
    }

    private void resetTargetAtPosition(Position pos) {
        Target.this.setScaleX(1);
        Target.this.setScaleY(1);
        Target.this.setScaleZ(1);

        Target.this.setPosition(pos);

        Target.this.setTranslateX(0);
        Target.this.setTranslateY(0);
        Target.this.setTranslateZ(0);
    }

    private void createBackAndForthTranlations(Position pos1, Position pos2, int length) {
        Duration animationLength = new Duration(Configuration.getInstance().getSpeedEffects() * length);

        Timeline translation1 = new Timeline(new KeyFrame(animationLength,
                new KeyValue(this.centerXProperty(), pos1.getX()), new KeyValue(this.centerYProperty(), pos1.getY())));

        Timeline translation2 = new Timeline(new KeyFrame(animationLength,
                new KeyValue(this.centerXProperty(), pos2.getX()), new KeyValue(this.centerYProperty(), pos2.getY())));

        translation1.setOnFinished(actionEvent -> {
            resetTargetAtPosition(pos1);

            currentTranslation = translation2;
            translation2.playFromStart();
        });

        translation2.setOnFinished(actionEvent -> {
            resetTargetAtPosition(pos2);

            currentTranslation = translation1;
            translation1.playFromStart();
        });

        setPosition(pos2);
        translation1.playFrom(animationLength.multiply(randomGen.nextDouble()));
        currentTranslation = translation1;
    }

    private void move() {
        final int length = randomGen.nextInt(2000) + 1000;// between 1 and 3 seconds

        Dimension2D dimension2D = randomPositionGenerator.getDimension2D();

        switch (gameVariant) {
        case 1: // random
            moveRandom(length);
            break;
        case 2: // vertical
            createBackAndForthTranlations(new Position(getCenterX(), getInitialRadius()),
                    new Position(getCenterX(), dimension2D.getHeight() - getInitialRadius()), length * 2);
            break;
        case 3: // horizontal
            createBackAndForthTranlations(new Position(getInitialRadius(), getCenterY()),
                    new Position(dimension2D.getWidth() - getInitialRadius(), getCenterY()), length * 2);
            break;
        case 4: // Diagonal \
            createBackAndForthTranlations(new Position(getInitialRadius(), getInitialRadius()),
                    new Position(dimension2D.getWidth() - getInitialRadius(),
                            dimension2D.getHeight() - getInitialRadius()),
                    length * 2);
            break;
        case 5: // Diagonal /
            createBackAndForthTranlations(new Position(dimension2D.getWidth() - getInitialRadius(), getInitialRadius()),
                    new Position(0, dimension2D.getHeight() - getInitialRadius()), length * 2);
            break;
        }

    }

    public Position getPointerPosition(Event e) {
        double mousePositionX = 0;
        double mousePositionY = 0;
        if (e instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) e;
            mousePositionX = mouseEvent.getX();
            mousePositionY = mouseEvent.getY();
        }
        if (e instanceof GazeEvent) {
            GazeEvent gazeEvent = (GazeEvent) e;
            mousePositionX = gazeEvent.getX();
            mousePositionY = gazeEvent.getY();
        }
        return new Position((int) mousePositionX, (int) mousePositionY);
    }

    private void enter(Event e) {

        stats.incNbGoals();

        Animation runningTranslation = currentTranslation;
        if (runningTranslation != null) {
            runningTranslation.stop();
        }

        // this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        Transition transition1 = createTransition1();

        Transition transition2 = createTransition2();

        Timeline childrenTimelineStart = new Timeline();
        Timeline childrenTimelineEnd = new Timeline();

        Position currentPositionWithTranslation = getCurrentPositionWithTranslation();

        Position pointerPosition = getPointerPosition(e);
        log.debug("pointerPosition = {}, currentPositionWithTranslation = {}", pointerPosition,
                currentPositionWithTranslation);

        for (Portrait childMiniBall : miniBallsPortraits) {
            childMiniBall.setPosition(currentPositionWithTranslation);
            childMiniBall.setOpacity(1);
            childMiniBall.setVisible(true);

            Position childBallTargetPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());

            childrenTimelineEnd.getKeyFrames()
                    .add(new KeyFrame(new Duration(1000), new KeyValue(childMiniBall.centerXProperty(),
                            childBallTargetPosition.getX(), Interpolator.EASE_OUT)));

            childrenTimelineEnd.getKeyFrames()
                    .add(new KeyFrame(new Duration(1000), new KeyValue(childMiniBall.centerYProperty(),
                            childBallTargetPosition.getY(), Interpolator.EASE_OUT)));

            childrenTimelineEnd.getKeyFrames()
                    .add(new KeyFrame(new Duration(1000), new KeyValue(childMiniBall.opacityProperty(), 0)));
        }

        Position newPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());

        Timeline selfTimeLine = new Timeline();

        selfTimeLine.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(radiusProperty(), radius)));

        selfTimeLine.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(centerXProperty(), newPosition.getX())));

        selfTimeLine.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(centerYProperty(), newPosition.getY())));

        selfTimeLine.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fillProperty(), new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true))));

        Transition transition4 = createTransition4();

        SequentialTransition sequence = new SequentialTransition(transition1, transition2, childrenTimelineStart,
                childrenTimelineEnd, selfTimeLine, transition4);

        sequence.setOnFinished(actionEvent -> {
            animationStopped = true;
            stats.notifyNewRoundReady();
            move();
        });

        sequence.play();

        try {
            Utils.playSound(audioClipResourceLocation);
        } catch (Exception exp) {

            log.warn("file doesn't exist : {}", audioClipResourceLocation);
            log.warn(exp.getMessage());
        }
    }

    private Transition createTransition1() {
        FadeTransition fadeTransition = new FadeTransition(new Duration(1), this);
        fadeTransition.setToValue(0.5);

        Timeline timeline1 = new Timeline();
        timeline1.getKeyFrames().add(new KeyFrame(new Duration(100), new KeyValue(radiusProperty(), ballRadius)));
        return new ParallelTransition(fadeTransition, timeline1);
    }

    private Transition createTransition2() {
        FadeTransition fadeTransition = new FadeTransition(new Duration(1), this);
        fadeTransition.setToValue(0);
        return fadeTransition;
    }

    private Transition createTransition4() {
        FadeTransition fadeTransition = new FadeTransition(new Duration(1), this);
        fadeTransition.setToValue(1);
        return fadeTransition;
    }
}
