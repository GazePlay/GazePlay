package net.gazeplay.games.bubbles;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.ProgressPortrait;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.ArrayList;
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

    private final ReplayablePseudoRandom randomGenerator;
    private final ReplayablePseudoRandom randomFragmentsGenerator = new ReplayablePseudoRandom();

    private final List<Circle> fragments;

    private final BubbleType type;

    private Timeline timeline;
    private Timeline timelineGrow;

    private double radius;

    public Target(final IGameContext gameContext, final RandomPositionGenerator randomPositionGenerator, final Stats stats,
                  final ImageLibrary imageLibrary, final BubblesGameVariant gameVariant, final Bubble gameInstance, final ReplayablePseudoRandom randomGenerator, BubbleType type) {
        super(gameContext.getConfiguration().getElementSize());
        this.gameInstance = gameInstance;
        this.gameContext = gameContext;
        this.randomGenerator = new ReplayablePseudoRandom();
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
            this.getButton().addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
                log.info("exited");
                timelineGrow.stop();
                timelineGrow.getKeyFrames().clear();
                timelineGrow.getKeyFrames().add(new KeyFrame(new Duration(100), new KeyValue(this.getButton().radiusProperty(), radius)));
                timelineGrow.play();
            });
            this.getButton().addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
                log.info("entered");
                timelineGrow.stop();
                timelineGrow.getKeyFrames().clear();
                timelineGrow.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(this.getButton().radiusProperty(), radius * 2)));
                timelineGrow.play();
            });
        }
        assignIndicatorUpdatable(enterEvent, gameContext);
        gameContext.getGazeDeviceManager().addEventFilter(this);
        active();


        createTarget();

        gameContext.start();
    }

    private EventHandler<Event> buildEvent() {

        return e -> {
            enter(e);
        };
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
    }

    private void enter(final Event e) {
        final double centerX = getLayoutX();
        final double centerY = getLayoutY();

        gameContext.getGazeDeviceManager().removeEventFilter(this);
        this.getChildren().remove(this);

        stats.incrementNumberOfGoalsReached();

        gameContext.updateScore(stats, gameInstance);

        explose(centerX, centerY); // instead of C to avoid wrong position of the explosion

        timeline.stop();

        moveTarget();
    }

    private void createTarget() {

        final Dimension2D screenDimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double maxRadius = Math.min(screenDimension.getWidth() / 12, screenDimension.getHeight() / 12);
        double minRadius = Math.min(screenDimension.getHeight() / 30, screenDimension.getWidth() / 30);
        radius = (maxRadius - minRadius) * randomGenerator.nextDouble() + minRadius;
        getButton().setRadius(radius);

        if (type == BubbleType.COLOR) {
            getButton().setFill(new Color(randomGenerator.nextDouble(), randomGenerator.nextDouble(), randomGenerator.nextDouble(), 0.9));
        } else {
            getButton().setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
        }
        setVisible(true);

        moveTarget();
    }

    private void moveTarget() {
        final javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double centerX = 0;
        double centerY = 0;

        final double timelength = ((maxTimeLength - minTimeLength) * randomGenerator.nextDouble() + minTimeLength) * 1000;

        timeline = new Timeline();

        double maxRadius = Math.min(dimension2D.getWidth() / 12, dimension2D.getHeight() / 12);
        double minRadius = Math.min(dimension2D.getHeight() / 30, dimension2D.getWidth() / 30);
        radius = (maxRadius - minRadius) * randomGenerator.nextDouble() + minRadius;
        getButton().setRadius(radius);

        if (this.gameVariant == BubblesGameVariant.TOP || this.gameVariant == BubblesGameVariant.TOP_FIX) {
            centerX = (dimension2D.getWidth() - radius) * randomGenerator.nextDouble() + radius;
            centerY = dimension2D.getHeight();
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(layoutYProperty(), -radius, Interpolator.EASE_IN)));
        } else if (this.gameVariant == BubblesGameVariant.BOTTOM || this.gameVariant == BubblesGameVariant.BOTTOM_FIX) {
            centerX = (dimension2D.getWidth() - radius) * randomGenerator.nextDouble() + radius;
            centerY = 0;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(layoutYProperty(), dimension2D.getHeight() + radius, Interpolator.EASE_IN)));
        } else if (this.gameVariant == BubblesGameVariant.RIGHT || this.gameVariant == BubblesGameVariant.RIGHT_FIX) {
            centerX = 0;
            centerY = (dimension2D.getHeight() - radius) * randomGenerator.nextDouble() + radius;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(layoutXProperty(), dimension2D.getWidth() + radius, Interpolator.EASE_IN)));
        } else if (this.gameVariant == BubblesGameVariant.LEFT || this.gameVariant == BubblesGameVariant.LEFT_FIX) {
            centerX = dimension2D.getWidth();
            centerY = (dimension2D.getHeight() - radius) * randomGenerator.nextDouble() + radius;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(layoutXProperty(), -radius, Interpolator.EASE_IN)));
        }


        setLayoutX(centerX);
        setLayoutY(centerY);

        timeline.setOnFinished(actionEvent -> {
            createTarget();
        });

        timeline.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());

        timeline.play();
    }

    private void decreaseSize() {
        final Dimension2D screenDimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        PauseTransition Wait = new PauseTransition(Duration.millis(5));
        Wait.setOnFinished(WaitEvent -> {
            if (this.getButton().getRadius() > Math.min(screenDimension.getWidth() / 20, screenDimension.getHeight() / 20)) {
                this.getButton().setRadius(this.getButton().getRadius() / Math.pow(1.005, gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty().getValue()));
            }
            decreaseSize();
        });
        Wait.play();
    }

}
