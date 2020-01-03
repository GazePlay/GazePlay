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
import javafx.stage.Screen;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
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

    private final boolean image;

    private final ImageLibrary imageLibrary;

    private final List<Circle> fragments;

    private final EventHandler<Event> enterEvent;

    private final BubblesGameVariant direction;

    public Bubble(IGameContext gameContext, BubbleType type, Stats stats, boolean useBackgroundImage, BubblesGameVariant direction) {
        this.gameContext = gameContext;
        this.type = type;
        this.stats = stats;
        this.image = useBackgroundImage;
        this.direction = direction;

        imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubDirectory("portraits"));

        if (useBackgroundImage) {

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            int i = (gameContext.getConfiguration().isBackgroundWhite()) ? 1 : 0;

            imageRectangle.setFill(new ImagePattern(new Image("data/bubble/images/underwater-treasures.jpg")));
            imageRectangle.setOpacity(1 - i * 0.9);

            gameContext.getChildren().add(imageRectangle);
        }

        gameContext.getChildren().add(this);

        this.fragments = buildFragments(type);

        this.getChildren().addAll(fragments);

        enterEvent = e -> {

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                // log.debug(e.getEventType());
                enter((Circle) e.getTarget());
                stats.incNbGoals();
                stats.notifyNewRoundReady();
            }
        };

    }

    @Override
    public void launch() {

        for (int i = 0; i < 10; i++) {

            newCircle();
        }

        stats.notifyNewRoundReady();

    }

    @Override
    public void dispose() {

    }

    private List<Circle> buildFragments(BubbleType bubbleType) {
        List<Circle> fragments = new ArrayList<>(nbFragments);

        for (int i = 0; i < nbFragments; i++) {

            Circle fragment = new Circle();
            fragment.setOpacity(1);
            fragment.setRadius(20);
            fragment.setVisible(true);
            fragment.setCenterX(-100);
            fragment.setCenterY(-100);

            if (bubbleType == BubbleType.COLOR) {
                fragment.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));
            } else {
                fragment.setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
            }

            fragments.add(fragment);
        }

        return fragments;
    }

    public void explose(double Xcenter, double Ycenter) {

        Timeline goToCenterTimeline = new Timeline();
        Timeline timeline = new Timeline();

        for (int i = 0; i < nbFragments; i++) {

            Circle fragment = fragments.get(i);

            fragment.setCenterX(Xcenter);
            fragment.setCenterY(Ycenter);
            fragment.setOpacity(1);

            goToCenterTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                new KeyValue(fragment.centerXProperty(), Xcenter, Interpolator.LINEAR)));
            goToCenterTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                new KeyValue(fragment.centerYProperty(), Ycenter, Interpolator.EASE_OUT)));
            goToCenterTimeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.opacityProperty(), 1)));

            double XendValue = Math.random() * Screen.getPrimary().getBounds().getWidth();
            double YendValue = Math.random() * Screen.getPrimary().getBounds().getHeight();

            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fragment.centerXProperty(), XendValue, Interpolator.LINEAR)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fragment.centerYProperty(), YendValue, Interpolator.EASE_OUT)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.opacityProperty(), 0)));
        }

        SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(goToCenterTimeline, timeline);
        sequence.play();

        if (Math.random() > 0.5) {
            String soundResource = "data/bubble/sounds/Large-Bubble-SoundBible.com-1084083477.mp3";
            try {
                ForegroundSoundsUtils.playSound(soundResource);
            } catch (Exception e) {

                log.warn("file doesn't exist : {}", soundResource);
                log.warn(e.getMessage());
            }
        } else {
            String soundResource = "data/bubble/sounds/Blop-Mark_DiAngelo-79054334.mp3";
            try {
                ForegroundSoundsUtils.playSound(soundResource);
            } catch (Exception e) {

                log.warn("file doesn't exist : {}", soundResource);
                log.warn(e.getMessage());
            }
        }

    }

    private void enter(Circle target) {

        double Xcenter = target.getCenterX();
        double Ycenter = target.getCenterY();

        gameContext.getGazeDeviceManager().removeEventFilter(target);
        this.getChildren().remove(target);

        explose(Xcenter, Ycenter); // instead of C to avoid wrong position of the explosion

        this.newCircle();
        stats.incNbGoals();
    }

    private void newCircle() {
        Circle circle = buildCircle();
        circle.toBack();

        this.getChildren().add(circle);
        this.gameContext.resetBordersToFront();

        gameContext.getGazeDeviceManager().addEventFilter(circle);

        circle.addEventFilter(MouseEvent.ANY, enterEvent);
        circle.addEventHandler(GazeEvent.ANY, enterEvent);

        moveCircle(circle);
    }

    private Circle buildCircle() {

        Circle C = new Circle();

        double radius = (maxRadius - minRadius) * Math.random() + minRadius;

        C.setRadius(radius);

        if (type == BubbleType.COLOR)
            C.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.9));
        else
            C.setFill(new ImagePattern(imageLibrary.pickRandomImage(), 0, 0, 1, 1, true));
        stats.incNbShots();
        stats.incNbShots();

        return C;
    }

    private void moveCircle(Circle circle) {
        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        double centerX = 0;
        double centerY = 0;

        double timelength = ((maxTimeLength - minTimeLength) * Math.random() + minTimeLength) * 1000;

        Timeline timeline = new Timeline();


        if (this.direction == BubblesGameVariant.TOP) {
            centerX = (dimension2D.getWidth() - maxRadius) * Math.random() + maxRadius;
            centerY = dimension2D.getHeight();
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(circle.centerYProperty(), 0 - maxRadius, Interpolator.EASE_IN)));
        } else if (this.direction == BubblesGameVariant.BOTTOM) {
            centerX = (dimension2D.getWidth() - maxRadius) * Math.random() + maxRadius;
            centerY = 0;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(circle.centerYProperty(), dimension2D.getHeight() + maxRadius, Interpolator.EASE_IN)));
        } else if (this.direction == BubblesGameVariant.RIGHT) {
            centerX = 0;
            centerY = (dimension2D.getHeight() - maxRadius) * Math.random() + maxRadius;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(circle.centerXProperty(), dimension2D.getWidth() + maxRadius, Interpolator.EASE_IN)));
        } else if (this.direction == BubblesGameVariant.LEFT) {
            centerX = dimension2D.getWidth();
            centerY = (dimension2D.getHeight() - maxRadius) * Math.random() + maxRadius;
            timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(timelength),
                    new KeyValue(circle.centerXProperty(), 0 - maxRadius, Interpolator.EASE_IN)));
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
