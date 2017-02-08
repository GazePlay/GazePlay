package creampie;

import creampie.event.TouchEvent;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

/**
 * Created by schwab on 17/08/2016.
 */
public class Hand extends Parent {

    private static final int size = 40;
    private static final int maxSize = 100;

    private ImageView background;
    private Scene scene;

    private ImageView pie;

    public Hand(Scene scene) {

        this.scene = scene;

        background = new ImageView(new Image("file:data/creampie/images/hand.png"));
        background.setFitHeight(maxSize);
        background.setPreserveRatio(true);
        this.getChildren().add(background);
        background.setTranslateX(scene.getWidth()/2);
        background.setTranslateY(scene.getHeight()-size);

        pie = new ImageView(new Image("file:data/creampie/images/gateau.png"));
        pie.setFitHeight(size);
        pie.setPreserveRatio(true);
        this.getChildren().add(pie);
        pie.setTranslateX(scene.getWidth()/2);
        pie.setTranslateY(scene.getHeight()-size);
        pie.toBack();

        this.addEventHandler(TouchEvent.TOUCH, (TouchEvent te) -> touch(te));
    }

    private void touch(TouchEvent te) {

        AudioClip plonkSound = new AudioClip("file:data/creampie/sounds/missile.mp3");
        plonkSound.play();

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(200),new KeyValue(background.fitHeightProperty(),size)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.translateXProperty(),te.x-maxSize)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.translateYProperty(),te.y-maxSize)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.fitHeightProperty(), maxSize*2)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.rotateProperty(), pie.getRotate()+360)));



        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(background.fitHeightProperty(),maxSize)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.fitHeightProperty(), size)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(background.translateXProperty(),scene.getWidth()/2)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(background.translateYProperty(),scene.getHeight()-maxSize)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.translateXProperty(),scene.getWidth()/2)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.translateYProperty(),scene.getHeight()-maxSize)));

        /*timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),new KeyValue(background.fitHeightProperty(),Maxsize)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),new KeyValue(pie.centerXProperty(),te.x)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),new KeyValue(pie.centerYProperty(),te.y)));


        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(background.fitHeightProperty(),size)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.centerXProperty(),scene.getWidth()/2)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.centerYProperty(),scene.getHeight()-size)));
*/
        SequentialTransition sequence = new SequentialTransition(timeline, timeline2);

        sequence.play();
    }


}
