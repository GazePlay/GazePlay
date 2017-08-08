package ninja;

import gaze.GazeEvent;
import gaze.GazeUtils;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Screen;
import utils.games.Portrait;
import javafx.util.Duration;
import utils.games.Utils;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by schwab on 26/12/2016.
 */
public class Target extends Portrait {

    EventHandler<Event> enterEvent;

    private boolean anniOff = true;

    private static int radius = 100;

    private static int ballRadius = 50;

    private static int nbBall = 20;

    private ArrayList<Portrait> portraits = new ArrayList(nbBall);

    public Target(Group root) {

        super(radius);

        for (int i = 0; i < nbBall; i++) {

            Portrait P = new Portrait(ballRadius);
            P.setOpacity(0);
            root.getChildren().add(P);
            portraits.add(P);
        }

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if((e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == GazeEvent.GAZE_MOVED)  && anniOff) {

                    anniOff = false;
                    enter();
                }
            }
        };

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        GazeUtils.addEventFilter(this);
        this.addEventHandler(GazeEvent.ANY, enterEvent);

        move();
    }


    private void move(){

        SequentialTransition sequence = new SequentialTransition();
        for (int i = 0; i < 40 ; i++){

            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(centerXProperty(), newX())));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(centerYProperty(), newY())));

            sequence.getChildren().add(timeline);
        }

        sequence.play();
    }


    private void enter(){

        this.removeEventHandler(MouseEvent.MOUSE_ENTERED, enterEvent);

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();
        Timeline timeline3 = new Timeline();
        Timeline timeline4 = new Timeline();


        timeline.getKeyFrames().add(new KeyFrame(new Duration(100),new KeyValue(radiusProperty(), ballRadius)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(100),new KeyValue(opacityProperty(), 0.5)));

        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(opacityProperty(), 0)));

        for (int i = 0; i < nbBall; i++) {

            Portrait P = portraits.get(i);

            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(P.centerXProperty(), getCenterX())));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(P.centerYProperty(), getCenterY())));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(P.opacityProperty(), 1)));

            double XendValue = Math.random()* Screen.getPrimary().getBounds().getWidth();
            double YendValue = Math.random()* Screen.getPrimary().getBounds().getHeight();
            timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000),new KeyValue(P.centerYProperty(), YendValue, Interpolator.EASE_OUT)));
            timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(P.centerXProperty(), XendValue, Interpolator.EASE_OUT)));
            timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(P.opacityProperty(), 0)));
        }

        timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(radiusProperty(), radius)));
        timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(centerXProperty(), newX())));
        timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(centerYProperty(), newY())));
        timeline3.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fillProperty(), new ImagePattern(newPhoto(), 0, 0, 1,1, true))));
        timeline4.getKeyFrames().add(new KeyFrame(new Duration(1000),new KeyValue(opacityProperty(), 1)));


        SequentialTransition sequence = new SequentialTransition(timeline,timeline2, timeline3, timeline4);

        sequence.play();

        sequence.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                anniOff = true;
                move();
                setVisible(true);
            }
        });

        Utils.playSound("data/ninja/sounds/2009.wav");
    }
}
