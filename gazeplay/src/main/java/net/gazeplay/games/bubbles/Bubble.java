package net.gazeplay.games.bubbles;

import gaze.GazeEvent;
import gaze.GazeUtils;
import javafx.animation.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.util.Duration;
import utils.games.stats.BubblesGamesStats;
import utils.games.Utils;

import java.util.ArrayList;

/**
 * Created by schwab on 28/08/2016.
 */
public class Bubble extends Parent {

    private int type;

    private final int maxRadius = 70;
    private final int minRadius = 30;

    private final int maxTimeLength = 7;
    private final int minTimeLength = 4;

    private final int nbFragments = 10; //number of little circles after explosion

    private ArrayList<Circle> fragments = new ArrayList(nbFragments);

    private final static Image[] photos = Utils.images(System.getProperties().getProperty("user.home")+ Utils.FILESEPARATOR +"GazePlay"+ Utils.FILESEPARATOR +"files"+ Utils.FILESEPARATOR +"images"+ Utils.FILESEPARATOR +"portraits");

    EventHandler<Event> enterEvent;
    Scene scene;

    public static final int PORTRAIT = 0;
    public static final int COLOR = 1;

    public Bubble(Scene scene, Group root, int type, BubblesGamesStats stats) {

        this.scene = scene;

        this.type = type;

        root.getChildren().add(this);

        for (int i = 0; i < nbFragments; i++) {

            Circle fragment = new Circle();

            fragments.add(fragment);

            fragment.setOpacity(1);
            fragment.setRadius(20);
            fragment.setVisible(true);
            fragment.setCenterX(-100);
            fragment.setCenterY(-100);


            if(type == COLOR)
                fragment.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));
            else
                fragment.setFill(new ImagePattern(newPhoto(),0,0,1,1, true));

        }

        this.getChildren().addAll(fragments);

        enterEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

               // log.info(e.getEventType() + " " + e.getTarget());
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    //log.info(e.getEventType());
                    enter((Circle) e.getTarget());
                    stats.incNbGoals();
                    stats.start();
                }
            }
        };

        for (int i = 0; i < 10; i++) {

            newCircle();
        }

        stats.start();
    }

    public void explose(Circle C){

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

        for (int i = 0; i < nbFragments; i++) {

            Circle fragment = fragments.get(i);

           /* fragment.setCenterX(C.getCenterX());
            fragment.setCenterY(C.getCenterY());
            fragment.setOpacity(1);*/
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.centerXProperty(), C.getCenterX(), Interpolator.LINEAR)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.centerYProperty(), C.getCenterY(), Interpolator.EASE_OUT)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.opacityProperty(), 1)));


            double XendValue = Math.random() * Screen.getPrimary().getBounds().getWidth();
            double YendValue = Math.random() * Screen.getPrimary().getBounds().getHeight();

            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.centerXProperty(), XendValue, Interpolator.LINEAR)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.centerYProperty(), YendValue, Interpolator.EASE_OUT)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.opacityProperty(), 0)));
        }

        SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(timeline, timeline2);
        sequence.play();

        ObservableList<Node> nodes = this.getChildren();

        timeline.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

              //  nodes.removeAll(fragments);
            }
        });

        if(Math.random()>0.5)
            Utils.playSound("data/bubble/sounds/Large-Bubble-SoundBible.com-1084083477.mp3");
        else
            Utils.playSound("data/bubble/sounds/Blop-Mark_DiAngelo-79054334.mp3");
    }

    private void enter(Circle target) {

        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(target.centerXProperty(), - maxRadius*5)));

        timeline.play();

        target.removeEventFilter(MouseEvent.ANY, enterEvent);

        target.removeEventFilter(GazeEvent.ANY, enterEvent);

        explose(target);
    }

    private void newCircle() {

        Circle C = buildCircle();

        this.getChildren().add(C);

        GazeUtils.addEventFilter(C);

        C.addEventFilter(MouseEvent.ANY, enterEvent);

        C.addEventHandler(GazeEvent.ANY, enterEvent);

        moveCircle(C);
    }

    private Circle buildCircle(){

        Circle C = new Circle();

        double radius = (maxRadius - minRadius) * Math.random() + minRadius;

        C.setRadius(radius);

        if(type == COLOR)
            C.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.7));
        else
            C.setFill(new ImagePattern(newPhoto(),0,0,1,1, true));

        return C;
    }

    private void moveCircle(Circle C) {

        double centerX = (scene.getWidth() - maxRadius) * Math.random() + maxRadius;
        double centerY = scene.getHeight();

        C.setCenterX(centerX);
        //C.setTranslateY((scene.getHeight() - maxRadius) * Math.random() + maxRadius);
        C.setCenterY(centerY);

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

    protected Image newPhoto(){

        return photos[((int)(photos.length*Math.random()))];

    }
}
