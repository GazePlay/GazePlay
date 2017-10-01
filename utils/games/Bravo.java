package utils.games;

import com.sun.glass.ui.Screen;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;

/**
 * Created by schwab on 30/10/2016.
 */
public class Bravo extends Rectangle{

    private static int length = 650;
    private static int nb = 5;


    public Bravo() {

        super(-10000, -10000, 0, 0);
        setFill(new ImagePattern(new Image("data/common/images/bravo.png")));
    }
/*
    public static Rectangle buildBravo() {

        Rectangle R = new Rectangle(-10000, -10000, 0, 0);
        R.setFill(new ImagePattern(new Image("file:data/common/images/bravo.png")));
        return R;
    }
*/
    public SequentialTransition win(){

        Screen screen = Screen.getScreens().get(0);

        int XBig = screen.getWidth()/4;
        int YBig = screen.getHeight()/8;

        int XSmall = screen.getWidth()/3;
        int YSmall = screen.getWidth()/6;

        int minWidth = screen.getWidth()/3;
        int maxWidth = screen.getWidth()/2;

        setX(XSmall);
        setY(YSmall);
        setWidth(minWidth);
        setHeight(minWidth);

        setTranslateX(0);

        this.setVisible(true);

        //this.toFront(); // bug when it is uncommented (with bloc at least).

        SequentialTransition sequence = new SequentialTransition();

        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(length), new KeyValue(xProperty(), XBig)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(length), new KeyValue(yProperty(), YBig)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(length), new KeyValue(widthProperty(), maxWidth)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(length), new KeyValue(heightProperty(), maxWidth)));

        sequence.getChildren().add(timeline);

        Timeline timeline2 = new Timeline();

        timeline2 = new Timeline();
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(length), new KeyValue(xProperty(), XSmall)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(length), new KeyValue(yProperty(), YSmall)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(length), new KeyValue(widthProperty(), minWidth)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(length), new KeyValue(heightProperty(), minWidth)));

        sequence.getChildren().add(timeline2);

        sequence.setCycleCount(5);

        sequence.play();

        Utils.playSound("data/common/sounds/applause.mp3");
        
        return sequence;
    }


}
