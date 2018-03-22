package net.gazeplay.games.ninja;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.Position;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.net.URL;
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

    private final Image[] availableImages;

    private final EventHandler<Event> enterEvent;

    private static final String audioClipResourceLocation = "data/ninja/sounds/2009.wav";

    private boolean animationStopped = true;

    private TranslateTransition currentTranslation;

    public Target(GameContext gameContext, RandomPositionGenerator randomPositionGenerator, Stats stats,
            Image[] availableImages) {
        super(radius, randomPositionGenerator, availableImages);

        this.randomPositionGenerator = randomPositionGenerator;
        this.stats = stats;
        this.availableImages = availableImages;

        this.miniBallsPortraits = generateMiniBallsPortraits(randomPositionGenerator, availableImages, nbBall);
        gameContext.getChildren().addAll(miniBallsPortraits);

        enterEvent = buildEvent();

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(this);
        this.addEventHandler(GazeEvent.ANY, enterEvent);

        move();

        stats.start();
    }

    private void playHitSound() {
        Utils.playSound(audioClipResourceLocation);
    }

    private List<Portrait> generateMiniBallsPortraits(RandomPositionGenerator randomPositionGenerator,
            Image[] availableImages, int count) {
        List<Portrait> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Portrait miniPortrait = new Portrait(ballRadius, randomPositionGenerator, availableImages);
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

    private void move() {
        // final int length = (int) (2000 * Math.random()) + 1000;// between 1 and 3 seconds

        Random r = new Random();
        final int length = r.nextInt(2000) + 1000;// between 1 and 3 seconds

        final Position currentPosition = new Position((int) getCenterX(), (int) getCenterY());
        final Position newPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());
        log.info("currentPosition = {}, newPosition = {}, length = {}", currentPosition, newPosition, length);

        TranslateTransition translation = new TranslateTransition(new Duration(length), this);
        translation.setByX(-this.getCenterX() + newPosition.getX());
        translation.setByY(-this.getCenterY() + newPosition.getY());
        translation.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                Target.this.setScaleX(1);
                Target.this.setScaleY(1);
                Target.this.setScaleZ(1);

                Target.this.setPosition(newPosition);

                Target.this.setTranslateX(0);
                Target.this.setTranslateY(0);
                Target.this.setTranslateZ(0);

                move();
            }
        });

        currentTranslation = translation;
        translation.play();
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

        Transition runningTranslation = currentTranslation;
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
        log.info("pointerPosition = {}, currentPositionWithTranslation = {}", pointerPosition,
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
                new KeyValue(fillProperty(), new ImagePattern(pickRandomImage(availableImages), 0, 0, 1, 1, true))));

        Transition transition4 = createTransition4();

        SequentialTransition sequence = new SequentialTransition(transition1, transition2, childrenTimelineStart,
                childrenTimelineEnd, selfTimeLine, transition4);

        sequence.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                animationStopped = true;
                stats.start();
                move();
            }
        });

        sequence.play();

        playHitSound();

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
