package net.gazeplay.games.ninja;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Screen;
import javafx.util.Duration;
import net.gazeplay.commons.gaze.GazeEvent;
import net.gazeplay.commons.gaze.GazeUtils;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schwab on 26/12/2016.
 */
public class Target extends Portrait {

    private static final int radius = 100;

    private static final int ballRadius = 50;

    private static final int nbBall = 20;

    private final RandomPositionGenerator randomPositionGenerator;

    private final Stats stats;

    private final List<Portrait> miniBallsPortraits;

    private final Image[] availableImages;

    private final EventHandler<Event> enterEvent;

    private boolean anniOff = true;

    public Target(Group root, RandomPositionGenerator randomPositionGenerator, Stats stats, Image[] availableImages) {
        super(radius, randomPositionGenerator, availableImages);

        this.randomPositionGenerator = randomPositionGenerator;
        this.stats = stats;
        this.availableImages = availableImages;

        this.miniBallsPortraits = generateMiniBallsPortraits(randomPositionGenerator, availableImages, nbBall);
        root.getChildren().addAll(miniBallsPortraits);

        enterEvent = buildEvent();

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        GazeUtils.addEventFilter(this);
        this.addEventHandler(GazeEvent.ANY, enterEvent);

        move();

        stats.start();
    }

    private List<Portrait> generateMiniBallsPortraits(RandomPositionGenerator randomPositionGenerator,
            Image[] availableImages, int count) {
        List<Portrait> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Portrait miniPortrait = new Portrait(ballRadius, randomPositionGenerator, availableImages);
            miniPortrait.setOpacity(0);
            result.add(miniPortrait);
        }
        return result;
    }

    private EventHandler<Event> buildEvent() {

        return e -> {

            // if((e.getEventType() == MouseEvent.MOUSE_ENTERED_TARGET ||e.getEventType() == MouseEvent.MOUSE_ENTERED ||
            // e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_ENTERED ||
            // e.getEventType() == GazeEvent.GAZE_MOVED) && anniOff) {

            if (anniOff
                    && (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED)) {

                anniOff = false;
                enter();
            }
        };
    }

    private void move() {

        Timeline timeline = new Timeline();
        int length = (int) (2000 * Math.random()) + 1000;// between 1 and 3 seconds

        Position newPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());

        timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(length), new KeyValue(centerXProperty(), newPosition.getX())));
        timeline.getKeyFrames()
                .add(new KeyFrame(new Duration(length), new KeyValue(centerYProperty(), newPosition.getY())));

        timeline.play();

        timeline.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                move();
            }
        });

    }

    private void enter() {

        stats.incNbGoals();

        // this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();
        Timeline timeline3 = new Timeline();
        Timeline timeline4 = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(100), new KeyValue(radiusProperty(), ballRadius)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(100), new KeyValue(opacityProperty(), 0.5)));

        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(opacityProperty(), 0)));

        for (int i = 0; i < nbBall; i++) {

            Portrait P = miniBallsPortraits.get(i);

            timeline2.getKeyFrames()
                    .add(new KeyFrame(new Duration(1), new KeyValue(P.centerXProperty(), getCenterX())));
            timeline2.getKeyFrames()
                    .add(new KeyFrame(new Duration(1), new KeyValue(P.centerYProperty(), getCenterY())));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(P.opacityProperty(), 1)));

            double XendValue = Math.random() * Screen.getPrimary().getBounds().getWidth();
            double YendValue = Math.random() * Screen.getPrimary().getBounds().getHeight();
            timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000),
                    new KeyValue(P.centerYProperty(), YendValue, Interpolator.EASE_OUT)));
            timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000),
                    new KeyValue(P.centerXProperty(), XendValue, Interpolator.EASE_OUT)));
            timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(P.opacityProperty(), 0)));
        }

        timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(radiusProperty(), radius)));

        Position newPosition = randomPositionGenerator.newRandomPosition(getInitialRadius());

        timeline3.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(centerXProperty(), newPosition.getX())));
        timeline3.getKeyFrames()
                .add(new KeyFrame(new Duration(1000), new KeyValue(centerYProperty(), newPosition.getY())));
        timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fillProperty(), new ImagePattern(pickRandomImage(availableImages), 0, 0, 1, 1, true))));
        timeline4.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(opacityProperty(), 1)));

        SequentialTransition sequence = new SequentialTransition(timeline, timeline2, timeline3, timeline4);

        sequence.play();

        sequence.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                anniOff = true;

                stats.start();
            }
        });

        Utils.playSound("data/ninja/sounds/2009.wav");
    }
}
