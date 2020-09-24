package net.gazeplay.games.bubbles;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 28/08/2016.
 */
@Slf4j
public class Bubble extends Parent implements GameLifeCycle {

    public static final String DIRECTION_TOP = "toTop";
    public static final String DIRECTION_BOTTOM = "toBottom";
    public static final String DIRECTION_LEFT = "toLeft";
    public static final String DIRECTION_RIGHT = "toRight";

    private static final int maxRadius = 70;
    private static final int minRadius = 30;

    private static final int maxTimeLength = 7;
    private static final int minTimeLength = 4;

    private static final int nbFragments = 10; // number of little circles after explosion

    private final IGameContext gameContext;

    private final BubbleType type;

    private final Stats stats;

    private final ImageLibrary imageLibrary;

    private List<Circle> fragments;

    private EventHandler<Event> enterEvent;

    private final BubblesGameVariant direction;

    private final ReplayablePseudoRandom randomGenerator;

    public Bubble(final IGameContext gameContext, final BubbleType type, final Stats stats, final BubblesGameVariant direction) {
        this.gameContext = gameContext;
        this.type = type;
        this.stats = stats;
        this.direction = direction;
        gameContext.startTimeLimiter();
        gameContext.startScoreLimiter();

        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("portraits"), randomGenerator);
    }

    public Bubble(final IGameContext gameContext, final BubbleType type, final Stats stats, final BubblesGameVariant direction, double gameSeed) {
        this.gameContext = gameContext;
        this.type = type;
        this.stats = stats;
        this.direction = direction;
        gameContext.startTimeLimiter();
        gameContext.startScoreLimiter();

        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("portraits"), randomGenerator);
    }

    void initBackground(boolean useBackgroundImage) {
        if (useBackgroundImage && gameContext.getConfiguration().isBackgroundEnabled()) {
            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            double imageRectangleOpacity = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Double>() {
                @Override
                public Double visitLight() {
                    return 0.1;
                }

                @Override
                public Double visitDark() {
                    return 1.d;
                }
            });

            imageRectangle.setFill(new ImagePattern(new Image("data/bubble/images/underwater-treasures.jpg")));
            imageRectangle.setOpacity(imageRectangleOpacity);

            gameContext.getChildren().add(imageRectangle);
        }
    }

    @Override
    public void launch() {
        this.getChildren().clear();
        initBackground(true);
        gameContext.getChildren().add(this);
        gameContext.setLimiterAvailable();

        this.fragments = buildFragments(type);

        this.getChildren().addAll(fragments);

        enterEvent = e -> {

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                enter((Circle) e.getTarget());
            }
        };

        gameContext.start();
        for (int i = 0; i < 10; i++) {

            newCircle();
        }


        this.fragments = buildFragments(type);

        this.getChildren().addAll(fragments);

        enterEvent = e -> {

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                enter((Circle) e.getTarget());
            }
        };


        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {
        enterEvent = e -> {
        };
        this.getChildren().clear();
    }

    private List<Circle> buildFragments(final BubbleType bubbleType) {
        final List<Circle> fragments = new ArrayList<>(nbFragments);

        for (int i = 0; i < nbFragments; i++) {

            final Circle fragment = new Circle();
            fragment.setOpacity(1);
            fragment.setRadius(20);
            fragment.setVisible(true);
            fragment.setCenterX(-100);
            fragment.setCenterY(-100);

            if (bubbleType == BubbleType.COLOR) {
                fragment.setFill(new Color(randomGenerator.nextDouble(), randomGenerator.nextDouble(), randomGenerator.nextDouble(), 1));
            } else {
                fragment.setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
            }

            fragments.add(fragment);
        }

        return fragments;
    }

    public void explose(final double centerX, final double centerY) {

        final Timeline goToCenterTimeline = new Timeline();
        final Timeline timeline = new Timeline();

        for (int i = 0; i < nbFragments; i++) {

            final Circle fragment = fragments.get(i);

            fragment.setCenterX(centerX);
            fragment.setCenterY(centerY);
            fragment.setOpacity(1);

            goToCenterTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                new KeyValue(fragment.centerXProperty(), centerX, Interpolator.LINEAR)));
            goToCenterTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                new KeyValue(fragment.centerYProperty(), centerY, Interpolator.EASE_OUT)));
            goToCenterTimeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.opacityProperty(), 1)));

            final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();
            final double endXValue = randomGenerator.nextDouble() * screenDimension.getWidth();
            final double endYValue = randomGenerator.nextDouble() * screenDimension.getHeight();

            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fragment.centerXProperty(), endXValue, Interpolator.LINEAR)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fragment.centerYProperty(), endYValue, Interpolator.EASE_OUT)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.opacityProperty(), 0)));
        }

        final SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(goToCenterTimeline, timeline);
        sequence.play();

        if (randomGenerator.nextDouble() > 0.5) {
            final String soundResource = "data/bubble/sounds/Large-Bubble-SoundBible.com-1084083477.mp3";
            gameContext.getSoundManager().add(soundResource);
        } else {
            final String soundResource = "data/bubble/sounds/Blop-Mark_DiAngelo-79054334.mp3";
            gameContext.getSoundManager().add(soundResource);
        }
    }

    private void enter(final Circle target) {

        final double centerX = target.getCenterX();
        final double centerY = target.getCenterY();

        gameContext.getGazeDeviceManager().removeEventFilter(target);
        this.getChildren().remove(target);

        stats.incrementNumberOfGoalsReached();

        gameContext.updateScore(stats,this);

        explose(centerX, centerY); // instead of C to avoid wrong position of the explosion

        this.newCircle();
    }

    private void newCircle() {
        final Circle circle = buildCircle();
        circle.toBack();

        this.getChildren().add(circle);
        this.gameContext.resetBordersToFront();
        stats.incrementNumberOfGoalsToReach();

        gameContext.getGazeDeviceManager().addEventFilter(circle);

        circle.addEventFilter(MouseEvent.ANY, enterEvent);
        circle.addEventHandler(GazeEvent.ANY, enterEvent);

        moveCircle(circle);
    }

    private Circle buildCircle() {

        final Circle newCircle = new Circle();
        final double radius = (maxRadius - minRadius) * randomGenerator.nextDouble() + minRadius;

        newCircle.setRadius(radius);

        if (type == BubbleType.COLOR) {
            newCircle.setFill(new Color(randomGenerator.nextDouble(), randomGenerator.nextDouble(), randomGenerator.nextDouble(), 0.9));
        } else {
            newCircle.setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
        }

        return newCircle;
    }

    private void moveCircle(final Circle circle) {
        final javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double centerX = 0;
        double centerY = 0;

        final double timelength = ((maxTimeLength - minTimeLength) * randomGenerator.nextDouble() + minTimeLength) * 1000;

        final Timeline timeline = new Timeline();


        if (this.direction == BubblesGameVariant.TOP) {
            centerX = (dimension2D.getWidth() - maxRadius) * randomGenerator.nextDouble() + maxRadius;
            centerY = dimension2D.getHeight();
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(circle.centerYProperty(), -maxRadius, Interpolator.EASE_IN)));
        } else if (this.direction == BubblesGameVariant.BOTTOM) {
            centerX = (dimension2D.getWidth() - maxRadius) * randomGenerator.nextDouble() + maxRadius;
            centerY = 0;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(circle.centerYProperty(), dimension2D.getHeight() + maxRadius, Interpolator.EASE_IN)));
        } else if (this.direction == BubblesGameVariant.RIGHT) {
            centerX = 0;
            centerY = (dimension2D.getHeight() - maxRadius) * randomGenerator.nextDouble() + maxRadius;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(circle.centerXProperty(), dimension2D.getWidth() + maxRadius, Interpolator.EASE_IN)));
        } else if (this.direction == BubblesGameVariant.LEFT) {
            centerX = dimension2D.getWidth();
            centerY = (dimension2D.getHeight() - maxRadius) * randomGenerator.nextDouble() + maxRadius;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(circle.centerXProperty(), -maxRadius, Interpolator.EASE_IN)));
        }


        circle.setCenterX(centerX);
        circle.setCenterY(centerY);

        timeline.setOnFinished(actionEvent -> {
            if (this.getChildren().contains(circle)) {
                gameContext.getGazeDeviceManager().removeEventFilter(circle);
                this.getChildren().remove(circle);
                newCircle();
            }
        });

        timeline.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        timeline.play();
    }

}
