package net.gazeplay.games.bubbles;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressPortrait;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class Target extends ProgressPortrait {

    private static final int maxTimeLength = 7;
    private static final int minTimeLength = 4;

    private static final int nbFragments = 10; // number of little circles after explosion

    private final IGameContext gameContext;

    private final Bubble gameInstance;

    private final BubblesGameVariant gameVariant;

    private final Stats stats;

    private final ImageLibrary imageLibrary;
    private final ArrayList<Bubble.Bloc> blocs;

    private final ReplayablePseudoRandom randomGenerator;
    private final ReplayablePseudoRandom randomFragmentsGenerator = new ReplayablePseudoRandom();

    private final List<Circle> fragments;

    private final BubbleType type;

    private Timeline timeline;
    private Timeline timelineGrow;

    private double radius;

    private EventHandler exitEvent;

    private PauseTransition wait;

    public Target(final IGameContext gameContext, final RandomPositionGenerator randomPositionGenerator, final Stats stats,
                  final ImageLibrary imageLibrary, final BubblesGameVariant gameVariant, final Bubble gameInstance, final ReplayablePseudoRandom randomGenerator, BubbleType type, ArrayList<Bubble.Bloc> blocs) {
        super(gameContext.getConfiguration().getElementSize());
        this.gameInstance = gameInstance;
        this.gameContext = gameContext;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.blocs = blocs;
        this.stats = stats;
        this.type = type;
        this.imageLibrary = imageLibrary;
        this.gameVariant = gameVariant;
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();

        this.fragments = buildFragments(type);
        gameContext.getChildren().addAll(fragments);

        EventHandler<Event> enterEvent = buildEvent();

        if (this.gameVariant.toString().endsWith("FIX")) {
            timelineGrow = new Timeline();
            exitEvent = e -> {
                timelineGrow.stop();
                getButton().setRadius(radius);
                setTranslateX(0);
                setTranslateY(0);
            };
            timelineGrow.setOnFinished(e -> {
                this.getButton().removeEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);
                this.setOpacity(0);
            });
            this.getButton().addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);
            this.getButton().addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
                timelineGrow.getKeyFrames().clear();
                timelineGrow.getKeyFrames().addAll(
                    new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength() - 100), new KeyValue(this.getButton().radiusProperty(), radius * 2)),
                    new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength() - 100), new KeyValue(this.getButton().radiusProperty(), radius * 2)),
                    new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength() - 100), new KeyValue(this.translateXProperty(), -this.getButton().radiusProperty().doubleValue())),
                    new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength() - 100), new KeyValue(this.translateYProperty(), -this.getButton().radiusProperty().doubleValue()))
                );
                timelineGrow.play();
            });

            wait = new PauseTransition(Duration.millis(15000 / gameContext.getConfiguration().getAnimationSpeedRatioProperty().doubleValue()));
            wait.setOnFinished(event -> {
                this.setOpacity(0);
                explose(); // instead of C to avoid wrong position of the explosion
                timeline.stop();
                moveTarget();
                this.getButton().addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);
                this.setOpacity(1);
                wait.setDuration(Duration.millis(15000 / gameContext.getConfiguration().getAnimationSpeedRatioProperty().doubleValue()));
                wait.play();
            });
            wait.play();
            assignIndicatorUpdatable(enterEvent, gameContext);
        } else {
            this.assignIndicator(enterEvent, 0);
        }
        gameContext.getGazeDeviceManager().addEventFilter(this);
        active();

        createTarget();

        gameContext.start();
    }

    private EventHandler<Event> buildEvent() {
        return this::enter;
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
                fragment.setFill(new Color(randomFragmentsGenerator.nextDouble(), randomFragmentsGenerator.nextDouble(), randomFragmentsGenerator.nextDouble(), 1));
            } else {
                fragment.setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
            }

            fragments.add(fragment);
        }

        return fragments;
    }

    public void explose() {
        stats.takeScreenShot();

        final Timeline goToCenterTimeline = new Timeline();
        final Timeline timeline = new Timeline();




        for (int i = 0; i < nbFragments; i++) {
            final Circle fragment = fragments.get(i);

            Position curentPosition = getCurrentCenterPositionWithTranslation();

            fragment.setCenterX(curentPosition.getX());
            fragment.setCenterY(curentPosition.getY());
            fragment.setOpacity(1);

            goToCenterTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                new KeyValue(fragment.centerXProperty(), curentPosition.getX(), Interpolator.LINEAR)));
            goToCenterTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                new KeyValue(fragment.centerYProperty(), curentPosition.getY(), Interpolator.EASE_OUT)));
            goToCenterTimeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.opacityProperty(), 1)));

            final Dimension2D sceneDimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
            final double endXValue = randomFragmentsGenerator.nextDouble() * sceneDimensions.getWidth();
            final double endYValue = randomFragmentsGenerator.nextDouble() * sceneDimensions.getHeight();

            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fragment.centerXProperty(), endXValue, Interpolator.LINEAR)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fragment.centerYProperty(), endYValue, Interpolator.EASE_OUT)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.opacityProperty(), 0)));
        }

        final SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(goToCenterTimeline, timeline);
        sequence.play();

        if (randomFragmentsGenerator.nextDouble() > 0.5) {
            final String soundResource = "data/bubble/sounds/Large-Bubble-SoundBible.com-1084083477.mp3";
            gameContext.getSoundManager().add(soundResource);
        } else {
            final String soundResource = "data/bubble/sounds/Blop-Mark_DiAngelo-79054334.mp3";
            gameContext.getSoundManager().add(soundResource);
        }

        if (blocs != null){
            if (blocs.size() != 0){
                Collections.shuffle(blocs);
                blocs.get(0).setOpacity(0);
                blocs.remove(0);
            }
        }
    }

    private void enter(final Event e) {

        if (this.gameVariant.toString().endsWith("FIX") && wait != null) {
            wait.stop();
            wait.setDuration(Duration.millis(15000 / gameContext.getConfiguration().getAnimationSpeedRatioProperty().doubleValue()));
            wait.play();
        }

        stats.incrementNumberOfGoalsReached();

        gameContext.updateScore(stats, gameInstance);

        explose(); // instead of C to avoid wrong position of the explosion

        timeline.stop();

        moveTarget();

        if (this.gameVariant.toString().endsWith("FIX")) {
            this.getButton().addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);
        }

        this.setOpacity(1);

    }

    private void createTarget() {

        final Dimension2D screenDimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        updateRadius(screenDimension);

        setVisible(true);
        moveTarget();
    }

    private void updateFillProperty() {
        if (type == BubbleType.COLOR) {
            getButton().setFill(new Color(randomGenerator.nextDouble(), randomGenerator.nextDouble(), randomGenerator.nextDouble(), 0.9));
        } else {
            getButton().setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
        }
    }

    private void updateRadius(Dimension2D screenDimension) {
        double maxRadius = Math.min(screenDimension.getWidth() / 12, screenDimension.getHeight() / 12);
        double minRadius = Math.min(screenDimension.getHeight() / 30, screenDimension.getWidth() / 30);
        radius = (maxRadius - minRadius) * randomGenerator.nextDouble() + minRadius;
        getButton().setRadius(radius);
    }

    private void moveTarget() {
        updateFillProperty();

        final javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double centerX = 0;
        double centerY = 0;

        final double timelength = ((maxTimeLength - minTimeLength) * randomGenerator.nextDouble() + minTimeLength) * 1000 / gameContext.getConfiguration().getAnimationSpeedRatio();

        timeline = new Timeline();

        updateRadius(dimension2D);

        if (this.gameVariant == BubblesGameVariant.TOP) {
            centerX = (dimension2D.getWidth() - 2 * radius) * randomGenerator.nextDouble();
            centerY = dimension2D.getHeight();
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(layoutYProperty(), -2 * radius, Interpolator.EASE_IN)));
        } else if (this.gameVariant == BubblesGameVariant.BOTTOM) {
            centerX = (dimension2D.getWidth() - 2 * radius) * randomGenerator.nextDouble();
            centerY = -2 * radius;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(layoutYProperty(), dimension2D.getHeight() + radius, Interpolator.EASE_IN)));
        } else if (this.gameVariant == BubblesGameVariant.RIGHT) {
            centerX = -2 * radius;
            centerY = (dimension2D.getHeight() - 2 * radius) * randomGenerator.nextDouble();
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(layoutXProperty(), dimension2D.getWidth() + 2 * radius, Interpolator.EASE_IN)));
        } else if (this.gameVariant == BubblesGameVariant.LEFT) {
            centerX = dimension2D.getWidth();
            centerY = (dimension2D.getHeight() - 2 * radius) * randomGenerator.nextDouble();
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(layoutXProperty(), -2 * radius, Interpolator.EASE_IN)));
        } else if (this.gameVariant == BubblesGameVariant.FIX) {
            centerX = radius + (dimension2D.getWidth() - 2 * radius) * randomGenerator.nextDouble();
            centerY = radius + (dimension2D.getHeight() - 2 * radius) * randomGenerator.nextDouble();
        }

        if (this.gameVariant != BubblesGameVariant.FIX) {
            timeline.setOnFinished(e -> moveTarget());
        }

        setLayoutX(centerX);
        setLayoutY(centerY);

        timeline.play();
    }

    private static class Bloc extends Rectangle {

        final int posX;
        final int posY;

        Bloc(final double x, final double y, final double width, final double height, final int posX, final int posY) {
            super(x, y, width, height);
            this.posX = posX;
            this.posY = posY;
        }

    }

}
