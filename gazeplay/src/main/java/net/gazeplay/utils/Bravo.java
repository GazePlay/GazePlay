package net.gazeplay.utils;

import com.sun.glass.ui.Screen;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import utils.games.Utils;

/**
 * Created by schwab on 30/10/2016.
 */
public class Bravo extends Rectangle {

    private static int duration = 650;
    private static int apparitionDuration = 2000;
    private static int nb = 5;
    private static Bravo bravo;

    private Bravo() {

        super(-10000, -10000, 0, 0);
        setFill(new ImagePattern(new Image("data/common/images/bravo.png")));
    }

    public static Bravo getBravo() {

        if (bravo == null)
            bravo = new Bravo();
        return bravo;
    }

    public SequentialTransition win() {

        Screen screen = Screen.getScreens().get(0);

        int XBig = screen.getWidth() / 4;
        int YBig = screen.getHeight() / 8;

        int XSmall = screen.getWidth() / 3;
        int YSmall = screen.getWidth() / 6;

        int minWidth = screen.getWidth() / 3;
        int maxWidth = screen.getWidth() / 2;

        setX(XSmall);
        setY(YSmall);
        setWidth(minWidth);
        setHeight(minWidth);

        setTranslateX(0);

        this.setVisible(true);

        this.setOpacity(0);

        // this.toFront(); // bug when it is uncommented (with bloc at least).

        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(apparitionDuration), new KeyValue(opacityProperty(), 1)));

        timeline.play();

        SequentialTransition sequence = new SequentialTransition();

        Timeline timeline2 = new Timeline();

        timeline2.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(xProperty(), XBig)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(yProperty(), YBig)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(widthProperty(), maxWidth)));
        timeline2.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(heightProperty(), maxWidth)));

        sequence.getChildren().add(timeline2);

        Timeline timeline3 = new Timeline();

        timeline3.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(xProperty(), XSmall)));
        timeline3.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(yProperty(), YSmall)));
        timeline3.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(widthProperty(), minWidth)));
        timeline3.getKeyFrames().add(new KeyFrame(new Duration(duration), new KeyValue(heightProperty(), minWidth)));

        sequence.getChildren().add(timeline3);

        sequence.setCycleCount(nb);

        sequence.play();

        Utils.playSound("data/common/sounds/applause.mp3");

        return sequence;
    }

}
