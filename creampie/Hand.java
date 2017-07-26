package creampie;

import creampie.event.TouchEvent;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;

/**
 * Created by schwab on 17/08/2016.
 */
public class Hand extends Parent {

    private static final int size = 40;
    private static final int maxSize = 100;

    //private ImageView background;
    private Rectangle hand;
    //private ImageView pie;
    private Rectangle pie;

    private Scene scene;



    private double handTranslateX = 0;
    private double handTranslateY = 0;
    private double pieTranslateX = 0;
    private double pieTranslateY = 0;

    public Hand(Scene scene) {

        this.scene = scene;

        handTranslateX = (scene.getWidth()-maxSize)/2;
        handTranslateY = scene.getHeight()-maxSize;
        pieTranslateX = (scene.getWidth()-size)/2;
        pieTranslateY = scene.getHeight()-maxSize;

        hand = new Rectangle(0, 0, maxSize, maxSize);

        hand.setFill(new ImagePattern(new Image("data/creampie/images/hand.png")));

        hand.setTranslateX(handTranslateX);
        hand.setTranslateY(handTranslateY);

        this.getChildren().add(hand);

        //System.out.println(ClassLoader.getSystemResourceAsStream("data/creampie/images/hand.png"));

        //background = new ImageView(new Image(ClassLoader.getSystemResourceAsStream("data/creampie/images/hand.png")));
       // background.setFitHeight(maxSize);
       // background.setPreserveRatio(true);



        //pie = new ImageView(new Image("file:data/creampie/images/gateau.png"));
        //pie = new ImageView(new Image(ClassLoader.getSystemResourceAsStream("data/creampie/images/gateau.png")));

        pie = new Rectangle(0, 0, size, size);

        pie.setFill(new ImagePattern(new Image("data/creampie/images/gateau.png")));

        pie.setTranslateX(pieTranslateX);
        pie.setTranslateY(pieTranslateY);

        this.getChildren().add(pie);
        pie.toBack();

        //pie.setFitHeight(size);
        //pie.setPreserveRatio(true);


        this.addEventHandler(TouchEvent.TOUCH, (TouchEvent te) -> touch(te));
    }

    private void touch(TouchEvent te) {

        URL url = ClassLoader.getSystemResource("data/creampie/sounds/missile.mp3");
        String path = url.toString();
        Media media = new Media(path);
        MediaPlayer mp = new MediaPlayer(media);
        mp.play();
/*
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream));
            clip.open(ais);
            clip.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
//        AudioClip plonkSound = new AudioClip("file:data/creampie/sounds/missile.mp3");

        //plonkSound.play();

        //InputStream inputStream = ClassLoader.getSystemResourceAsStream("data/creampie/sounds/missile.mp3");

       /*

        URL ressource = getClass().getResource("data/creampie/sounds/missile.mp3");
        System.out.println(ressource);
        AudioClip plonkSound = new AudioClip(ressource.toString());
        plonkSound.play();*/


        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

       // timeline.getKeyFrames().add(new KeyFrame(new Duration(200),new KeyValue(background.fitHeightProperty(),size)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(200),new KeyValue(hand.heightProperty(),size)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(200),new KeyValue(hand.widthProperty(), size)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.translateXProperty(),te.x-maxSize)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.translateYProperty(),te.y-maxSize)));
        //timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.fitHeightProperty(), maxSize*2)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.heightProperty(),maxSize*2)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.widthProperty(), maxSize*2)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000),new KeyValue(pie.rotateProperty(), pie.getRotate()+360)));



     //   timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(background.fitHeightProperty(),maxSize)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(hand.heightProperty(),maxSize)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(hand.widthProperty(), maxSize)));
        //timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.fitHeightProperty(), size)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.heightProperty(),size)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.widthProperty(), size)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(hand.translateXProperty(),handTranslateX)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(hand.translateYProperty(),handTranslateY)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.translateXProperty(),pieTranslateX)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(1),new KeyValue(pie.translateYProperty(),pieTranslateY)));

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
