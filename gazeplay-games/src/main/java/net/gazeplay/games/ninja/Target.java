package net.gazeplay.games.ninja;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.LevelsReport;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressPortrait;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by schwab on 26/12/2016.
 */
@Slf4j
public class Target extends ProgressPortrait {

    private static final int nbBall = 10;

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

    private LevelsReport levelsReport;

    private long length;

    private final String variantType;
    private final EventHandler<Event> enterEvent;


    public Target(final IGameContext gameContext, final RandomPositionGenerator randomPositionGenerator, final Stats stats,
                  final ImageLibrary imageLibrary, final NinjaGameVariant gameVariant, final Ninja gameInstance, final ReplayablePseudoRandom randomGenerator,final RoundsDurationReport roundsDurationReport, LevelsReport levelsReport,  int length) {
        super(gameContext.getConfiguration().getElementSize());
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
        this.levelsReport = levelsReport;
        this.length = length;
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

        assignIndicatorUpdatable(enterEvent, gameContext);
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

    private void moveRandom(final long length) {

        final Position currentPosition = new Position((int) getLayoutX(), (int) getLayoutY());

        final Position newPosition = randomPositionGenerator.newRandomPosition(gameContext.getConfiguration().getElementSize());
        resetTargetAtPosition(currentPosition);
        log.debug("currentPosition = {}, newPosition = {}, length = {}", currentPosition, newPosition, length);
        long finalLength;

        if (variantType.equals("Random Dynamic")) {

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            double height = dimension2D.getHeight();
            double distance = Math.sqrt(Math.pow(currentPosition.getX() - newPosition.getX(), 2) + Math.pow(currentPosition.getY() - newPosition.getY(), 2));
            int ratio = (int) (length / height);
            int lengthR = (int) (distance * ratio);

            log.debug("length = {}", length);
            log.debug("lengthR = {}, ratio = {}, distance = {}", lengthR, ratio, distance);

            if (ratio != 0)
                finalLength = lengthR;
            else
                finalLength = (int) distance;
        } else
            finalLength = length;
        final TranslateTransition translation = new TranslateTransition(
            new Duration(finalLength), this);
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

    private void createBackAndForthTranslations(final Position pos1, final Position pos2, final long length) {

        final Timeline translation1 = new Timeline(new KeyFrame(new Duration(length),
            new KeyValue(this.layoutXProperty(), pos1.getX()), new KeyValue(this.layoutYProperty(), pos1.getY())));
        translation1.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        final Timeline translation2 = new Timeline(new KeyFrame(new Duration(length),
            new KeyValue(this.layoutXProperty(), pos2.getX()), new KeyValue(this.layoutYProperty(), pos2.getY())));
        translation2.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        log.debug("currentPosition = {}, newPosition = {}, length = {}", pos1, pos2, length);
        double distance = Math.sqrt(Math.pow(pos1.getX() - pos2.getX(), 2) + Math.pow(pos1.getY() - pos2.getY(), 2));
        log.debug("distance = {}", distance);

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
        int lengthRandom = randomGen.nextInt(2000) + 1000;// between 1 and 3 seconds

        final Dimension2D dimension2D = randomPositionGenerator.getDimension2D();

        if (variantType.contains("Dynamic")) {
            if (500 < length && length < 12000) {
                int compare = 0;
                List<Long> listOfDurationBetweenGoals = roundsDurationReport.getOriginalDurationsBetweenGoals();
                int sizeOfList = listOfDurationBetweenGoals.size();
                if (sizeOfList % 3 == 0 && sizeOfList != 0) {
                    for (int i = 0; i < 3; i++) {
                        log.debug("DurationBetweenGoals.get(sizeOfList - 1 - i) = {}", listOfDurationBetweenGoals.get(sizeOfList - 1 - i));
                        if (listOfDurationBetweenGoals.get(sizeOfList - 1 - i) <= 1000) compare++;
                        if (listOfDurationBetweenGoals.get(sizeOfList - 1 - i) >= 2000) compare--;

                    }
                    if (compare == 3 && length > 600){
                        length -= 400;
                    }
                    if (compare == -3 && length < 11800){
                        length += 400;
                    }
                }
            }
            levelsReport.addRoundLevel(length);
        }

        switch (gameVariant) {
            case RANDOM: // random
                moveRandom(lengthRandom);
                break;
            case VERTICAL: // vertical
                createBackAndForthTranslations(new Position(getLayoutX(), gameContext.getConfiguration().getElementSize()),
                    new Position(getLayoutX(), dimension2D.getHeight() - gameContext.getConfiguration().getElementSize()), lengthRandom * 2);
                break;
            case HORIZONTAL: // horizontal
                createBackAndForthTranslations(new Position(gameContext.getConfiguration().getElementSize(), getLayoutY()),
                    new Position(dimension2D.getWidth() - gameContext.getConfiguration().getElementSize(), getLayoutY()), lengthRandom * 2);
                break;
            case DIAGONAL_UPPER_LEFT_TO_LOWER_RIGHT: // Diagonal \
                createBackAndForthTranslations(new Position(gameContext.getConfiguration().getElementSize(), gameContext.getConfiguration().getElementSize()),
                    new Position(dimension2D.getWidth() - gameContext.getConfiguration().getElementSize(),
                        dimension2D.getHeight() - gameContext.getConfiguration().getElementSize()),
                    lengthRandom * 2);
                break;
            case DIAGONAL_UPPER_RIGHT_TO_LOWER_LEFT: // Diagonal /
                createBackAndForthTranslations(new Position(dimension2D.getWidth() - gameContext.getConfiguration().getElementSize(), gameContext.getConfiguration().getElementSize()),
                    new Position(0, dimension2D.getHeight() - gameContext.getConfiguration().getElementSize()), lengthRandom * 2);
                break;

            case DYNAMIC_RANDOM:
                moveRandom(length);
                break;
            case DYNAMIC_VERTICAL: // vertical
                createBackAndForthTranslations(new Position(getLayoutX(), gameContext.getConfiguration().getElementSize()),
                    new Position(getLayoutX(), dimension2D.getHeight() - gameContext.getConfiguration().getElementSize()), length);
                break;
            case DYNAMIC_HORIZONTAL: // horizontal
                createBackAndForthTranslations(new Position(gameContext.getConfiguration().getElementSize(), getLayoutX()),
                    new Position(dimension2D.getWidth() - gameContext.getConfiguration().getElementSize(), getLayoutX()), length);
                break;
            case DYNAMIC_DIAGONAL_UPPER_LEFT_TO_LOWER_RIGHT: // Diagonal \
                createBackAndForthTranslations(new Position(gameContext.getConfiguration().getElementSize(), gameContext.getConfiguration().getElementSize()),
                    new Position(dimension2D.getWidth() - gameContext.getConfiguration().getElementSize(),
                        dimension2D.getHeight() - gameContext.getConfiguration().getElementSize()),
                    length);
                break;
            case DYNAMIC_DIAGONAL_UPPER_RIGHT_TO_LOWER_LEFT: // Diagonal /
                createBackAndForthTranslations(new Position(dimension2D.getWidth() - gameContext.getConfiguration().getElementSize(), gameContext.getConfiguration().getElementSize()),
                    new Position(0, dimension2D.getHeight() - gameContext.getConfiguration().getElementSize()), length);
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
        timeline1.getKeyFrames().add(new KeyFrame(new Duration(100), new KeyValue(getButton().radiusProperty(), gameContext.getConfiguration().getElementSize() / 2)));
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
