package samplesAmela;

import gaze.GazeEvent;
import gaze.GazeUtils;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Created by schwab on 28/08/2016.
 */
public class Bubble extends Parent {

    private final int maxRadius = 70;
    private final int minRadius = 30;

    private final int maxTimeLength = 7;
    private final int minTimeLength = 4;

    EventHandler<Event> enterEvent;
    Scene scene;

    public Bubble(Scene scene) {

        this.scene = scene;

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

               // System.out.println(e.getEventType() + " " + e.getTarget());
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    //System.out.println(e.getEventType());
                    enter((Circle) e.getTarget());
                }
            }
        };

        for (int i = 0; i < 20; i++) {

            newCircle();
        }
    }

    private void enter(Circle target) {

        //System.out.println("enter");

        //newCircle();

        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(target.opacityProperty(), 0)));

        timeline.play();


    }

    private void newCircle() {

        Circle C = new Circle();

        this.getChildren().add(C);

        GazeUtils.addEventFilter(C);

        C.addEventFilter(MouseEvent.ANY, enterEvent);

        C.addEventHandler(GazeEvent.ANY, enterEvent);

        moveCircle(C);
    }

    private void moveCircle(Circle C) {

        double centerX = (scene.getWidth() - maxRadius) * Math.random() + maxRadius;
        double centerY = scene.getHeight();

        C.setCenterX(centerX);
        //C.setTranslateY((scene.getHeight() - maxRadius) * Math.random() + maxRadius);
        C.setCenterY(centerY);
        double radius = (maxRadius - minRadius) * Math.random() + minRadius;
        C.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));
        C.setRadius(radius);

        Timeline timeline = new Timeline();

        double timelength = ((maxTimeLength - minTimeLength) * Math.random() + minTimeLength) * 1000;

        timeline.getKeyFrames().add(new KeyFrame(new Duration(timelength), new KeyValue(C.centerYProperty(), 0 - maxRadius, Interpolator.EASE_IN)));

       /* SequentialTransition sequence = new SequentialTransition();

        for(int i = 0; i < 10; i++) {
            sequence.getChildren().add(new KeyFrame(new Duration(timelength / 10), new KeyValue(C.centerXProperty(), centerX - 100)));
            sequence.getChildren().add(new KeyFrame(new Duration(timelength / 10), new KeyValue(C.centerXProperty(), centerX + 100)));
        }*/

        timeline.play();

        timeline.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                //moveCircle(C);
                newCircle();
            }
        });
    }
}
