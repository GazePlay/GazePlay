package net.gazeplay.games.bubbles;

import javafx.animation.*;
import javafx.event.ActionEvent;
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
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by schwab on 28/08/2016.
 */
@Slf4j
public class Bubble extends Parent implements GameLifeCycle {

    private static final int maxRadius = 70;
    private static final int minRadius = 30;

    private static final int maxTimeLength = 7;
    private static final int minTimeLength = 4;

    private static final int nbFragments = 10; // number of little circles after explosion

    private final GameContext gameContext;

    private final BubbleType type;

    private final Stats stats;

    private final boolean image;

    private final Image[] photos;

    private final List<Circle> fragments;

    private final EventHandler<Event> enterEvent;

    public Bubble(GameContext gameContext, BubbleType type, Stats stats, boolean useBackgroundImage) {
        this.gameContext = gameContext;
        this.type = type;
        this.stats = stats;
        this.image = useBackgroundImage;

        photos = Utils.images(Utils.getImagesFolder() + "portraits" + Utils.FILESEPARATOR);

        if (useBackgroundImage) {

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            imageRectangle.setFill(new ImagePattern(new Image("data/bubble/images/underwater-treasures.jpg")));
            gameContext.getChildren().add(imageRectangle);
        }

        gameContext.getChildren().add(this);

        this.fragments = buildFragments(type);

        this.getChildren().addAll(fragments);

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    // log.info(e.getEventType());
                    enter((Circle) e.getTarget());
                    stats.incNbGoals();
                    stats.start();
                }
            }
        };

    }

    @Override
    public void launch() {

        for (int i = 0; i < 10; i++) {

            newCircle();
        }

        stats.start();

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
                fragment.setFill(new ImagePattern(newPhoto(), 0, 0, 1, 1, true));
            }

            fragments.add(fragment);
        }

        return fragments;
    }

    public void explose(double Xcenter, double Ycenter) {

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

        for (int i = 0; i < nbFragments; i++) {

            Circle fragment = fragments.get(i);

            /*
             * fragment.setCenterX(C.getCenterX()); fragment.setCenterY(C.getCenterY()); fragment.setOpacity(1);
             */
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(fragment.centerXProperty(), Xcenter, Interpolator.LINEAR)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(fragment.centerYProperty(), Ycenter, Interpolator.EASE_OUT)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.opacityProperty(), 1)));

            double XendValue = Math.random() * Screen.getPrimary().getBounds().getWidth();
            double YendValue = Math.random() * Screen.getPrimary().getBounds().getHeight();

            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000),
                    new KeyValue(fragment.centerXProperty(), XendValue, Interpolator.LINEAR)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000),
                    new KeyValue(fragment.centerYProperty(), YendValue, Interpolator.EASE_OUT)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.opacityProperty(), 0)));
        }

        SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(timeline, timeline2);
        sequence.play();

        // ObservableList<Node> nodes = this.getChildren();

        timeline.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                // nodes.removeAll(fragments);
            }
        });

        if (Math.random() > 0.5)
            Utils.playSound("data/bubble/sounds/Large-Bubble-SoundBible.com-1084083477.mp3");
        else
            Utils.playSound("data/bubble/sounds/Blop-Mark_DiAngelo-79054334.mp3");
    }

    private void enter(Circle target) {

        double Xcenter = target.getCenterX();
        double Ycenter = target.getCenterY();

        Timeline timeline = new Timeline();

        timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(1), new KeyValue(target.centerXProperty(), -maxRadius * 5)));

        timeline.play();

        target.removeEventFilter(MouseEvent.ANY, enterEvent);

        target.removeEventFilter(GazeEvent.ANY, enterEvent);

        explose(Xcenter, Ycenter); // instead of C to avoid wrong position of the explosion
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
            C.setFill(new ImagePattern(newPhoto(), 0, 0, 1, 1, true));

        return C;
    }

    private void moveCircle(Circle circle) {
        javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);

        double centerX = (dimension2D.getWidth() - maxRadius) * Math.random() + maxRadius;
        double centerY = dimension2D.getHeight();

        circle.setCenterX(centerX);
        // circle.setTranslateY((scene.getHeight() - maxRadius) * Math.random() + maxRadius);
        circle.setCenterY(centerY);

        double timelength = ((maxTimeLength - minTimeLength) * Math.random() + minTimeLength) * 1000;

        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(timelength),
                new KeyValue(circle.centerYProperty(), 0 - maxRadius, Interpolator.EASE_IN)));

        timeline.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                // moveCircle(circle);
                newCircle();
            }
        });

        timeline.play();
    }

    protected Image newPhoto() {

        return photos[new Random().nextInt(photos.length)];

    }
}
