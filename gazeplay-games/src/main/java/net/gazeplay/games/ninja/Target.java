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
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.Position;
import net.gazeplay.components.RandomPositionGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 26/12/2016.
 */
@Slf4j
public class Target extends Portrait {

    private final int radius;

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

    private RoundsDurationReport roundsDurationReport;

    private int length;

    private final String variantType;
    //private List<Long> durationBetweenGoals;

    public Target(final IGameContext gameContext, final RandomPositionGenerator randomPositionGenerator, final Stats stats,
                  final ImageLibrary imageLibrary, final NinjaGameVariant gameVariant, final Ninja gameInstance, final ReplayablePseudoRandom randomGenerator, int radius, RoundsDurationReport roundsDurationReport, int length) {
        super(radius, randomPositionGenerator, imageLibrary);

        this.radius = radius;
        this.gameInstance = gameInstance;
        this.gameContext = gameContext;
        this.randomPositionGenerator = randomPositionGenerator;
        this.variantType = gameVariant.getLabel();

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
        this.roundsDurationReport = roundsDurationReport;
        this.length = length;
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();

        this.miniBallsPortraits = generateMiniBallsPortraits(imageLibrary, nbBall);
        gameContext.getChildren().addAll(miniBallsPortraits);

        final EventHandler<Event> enterEvent = buildEvent();

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        gameContext.getGazeDeviceManager().addEventFilter(this);
        this.addEventHandler(GazeEvent.ANY, enterEvent);

        move();
        gameContext.start();
    }

    private List<Portrait> generateMiniBallsPortraits(final ImageLibrary imageLibrary, final int count) {
        final List<Portrait> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final Portrait miniPortrait = new Portrait(radius/2, randomMiniBallsPositionGenerator, imageLibrary);
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
        int finalLength;

        if (variantType.equals("Random Dynamic")) {

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            double height = dimension2D.getHeight();
            double distance = Math.sqrt(Math.pow(currentPosition.getX() - newPosition.getX(), 2) + Math.pow(currentPosition.getY() - newPosition.getY(), 2));
            int ratio = (int) (length / height);
            int lengthR = (int) (distance * ratio);

            log.info("length = {}", length);
            log.info("lengthR = {}, ratio = {}, distance = {}", lengthR, ratio, distance);

            if (ratio != 0)
                finalLength = lengthR;
            else
                finalLength = (int)distance;
        } else
            finalLength = length;
        final TranslateTransition translation = new TranslateTransition(
            new Duration(finalLength), this);
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

    private void createBackAndForthTranslations(final Position pos1, final Position pos2, final int length) {

        final Timeline translation1 = new Timeline(new KeyFrame(new Duration(length),
            new KeyValue(this.centerXProperty(), pos1.getX()), new KeyValue(this.centerYProperty(), pos1.getY())));
        translation1.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        final Timeline translation2 = new Timeline(new KeyFrame(new Duration(length),
            new KeyValue(this.centerXProperty(), pos2.getX()), new KeyValue(this.centerYProperty(), pos2.getY())));
        translation2.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        log.info("currentPosition = {}, newPosition = {}, length = {}", pos1, pos2, length);
        double distance = Math.sqrt(Math.pow(pos1.getX()- pos2.getX(),2) + Math.pow(pos1.getY() - pos2.getY(),2));
        log.info("distance = {}", distance);

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
        int lengthRandom = randomGen.nextInt(2000) + 1000;// between 1 and 3 seconds

        final Dimension2D dimension2D = randomPositionGenerator.getDimension2D();

        if (variantType.contains("Dynamic")){
            if (500 < length && length < 12000) {
                int compare = 0;
                List<Long> listOfDurationBetweenGoals = roundsDurationReport.getOriginalDurationsBetweenGoals();
                int sizeOfList = listOfDurationBetweenGoals.size();
                if (sizeOfList % 3 == 0 && sizeOfList != 0) {
                    for (int i = 0; i < 3; i++) {
                        log.info("DurationBetweenGoals.get(sizeOfList - 1 - i) = {}", listOfDurationBetweenGoals.get(sizeOfList - 1 - i));
                        if (listOfDurationBetweenGoals.get(sizeOfList - 1 - i) <= 1000) compare++;
                        if (listOfDurationBetweenGoals.get(sizeOfList - 1 - i) >= 2000) compare--;

                    }
                    if (compare == 3 && length > 600) length -= 400;
                    if (compare == -3 && length < 11800) length += 400;
                }
            }
        }

        switch (gameVariant) {
            case RANDOM: // random
                moveRandom(lengthRandom);
                break;
            case VERTICAL: // vertical
                createBackAndForthTranslations(new Position(getCenterX(), getInitialRadius()),
                    new Position(getCenterX(), dimension2D.getHeight() - getInitialRadius()), lengthRandom*2);
                break;
            case HORIZONTAL: // horizontal
                createBackAndForthTranslations(new Position(getInitialRadius(), getCenterY()),
                    new Position(dimension2D.getWidth() - getInitialRadius(), getCenterY()), lengthRandom*2);
                break;
            case DIAGONAL_UPPER_LEFT_TO_LOWER_RIGHT: // Diagonal \
                createBackAndForthTranslations(new Position(getInitialRadius(), getInitialRadius()),
                    new Position(dimension2D.getWidth() - getInitialRadius(),
                        dimension2D.getHeight() - getInitialRadius()),
                    lengthRandom*2);
                break;
            case DIAGONAL_UPPER_RIGHT_TO_LOWER_LEFT: // Diagonal /
                createBackAndForthTranslations(new Position(dimension2D.getWidth() - getInitialRadius(), getInitialRadius()),
                    new Position(0, dimension2D.getHeight() - getInitialRadius()), lengthRandom*2);
                break;

            case DYNAMIC_RANDOM:
                moveRandom(length);
                break;
            case DYNAMIC_VERTICAL: // vertical
                createBackAndForthTranslations(new Position(getCenterX(), getInitialRadius()),
                    new Position(getCenterX(), dimension2D.getHeight() - getInitialRadius()), length);
                break;
            case DYNAMIC_HORIZONTAL: // horizontal
                createBackAndForthTranslations(new Position(getInitialRadius(), getCenterY()),
                    new Position(dimension2D.getWidth() - getInitialRadius(), getCenterY()), length);
                break;
            case DYNAMIC_DIAGONAL_UPPER_LEFT_TO_LOWER_RIGHT: // Diagonal \
                createBackAndForthTranslations(new Position(getInitialRadius(), getInitialRadius()),
                    new Position(dimension2D.getWidth() - getInitialRadius(),
                        dimension2D.getHeight() - getInitialRadius()),
                    length);
                break;
            case DYNAMIC_DIAGONAL_UPPER_RIGHT_TO_LOWER_LEFT: // Diagonal /
                createBackAndForthTranslations(new Position(dimension2D.getWidth() - getInitialRadius(), getInitialRadius()),
                    new Position(0, dimension2D.getHeight() - getInitialRadius()), length);
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

        gameContext.updateScore(stats,gameInstance);

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

            final Position childBallTargetPosition = randomMiniBallsPositionGenerator.newRandomPosition(getInitialRadius());

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
        timeline1.getKeyFrames().add(new KeyFrame(new Duration(100), new KeyValue(radiusProperty(), radius/2)));
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
