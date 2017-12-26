package net.gazeplay.commons.utils;

import com.sun.glass.ui.Screen;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;

import java.net.URL;

/**
 * Created by schwab on 30/10/2016.
 */
public class Bravo extends Rectangle {

    private static final int duration = 1300;

    private static final int nb = 5;

    private static final int apparitionDuration = duration * nb;

    private static final String pictureResourceLocation = "data/common/images/bravo.png";

    private static final String soundResourceLocation = "data/common/sounds/applause.mp3";

    @Getter
    private static final Bravo bravo = new Bravo();

    private final URL soundResourceUrl;

    private Bravo() {
        super(0, 0, 0, 0);

        ClassLoader classLoader = this.getClass().getClassLoader();

        soundResourceUrl = classLoader.getResource(soundResourceLocation);
    }

    public void playWinTransition(EventHandler<ActionEvent> onFinishedEventHandler) {
        resetState();

        FadeTransition fadeInTransition = new FadeTransition(new Duration(apparitionDuration), this);
        fadeInTransition.setFromValue(0.0);
        fadeInTransition.setToValue(1.0);
        fadeInTransition.setCycleCount(1);
        fadeInTransition.setAutoReverse(false);

        Transition scaleTransition = createScaleTransition();

        AudioClip soundClip = new AudioClip(soundResourceUrl.toExternalForm());

        ParallelTransition fullTransition = new ParallelTransition();

        fullTransition.getChildren().add(fadeInTransition);
        fullTransition.getChildren().add(scaleTransition);

        soundClip.play(0.2);

        fullTransition.setOnFinished(onFinishedEventHandler);

        fullTransition.play();
    }

    private void resetState() {
        setFill(new ImagePattern(new Image(pictureResourceLocation)));

        Screen screen = Screen.getScreens().get(0);

        int positionX = screen.getWidth() / 3;
        int positionY = screen.getWidth() / 6;

        int initialWidth = screen.getWidth() / 4;

        setX(positionX);
        setY(positionY);
        setWidth(initialWidth);
        setHeight(initialWidth);

        setTranslateX(0);
        setScaleX(1);
        setScaleY(1);
        setScaleZ(1);

        setOpacity(0);

        toFront(); // bug when it is uncommented (with bloc at least).

        // set visible only after all other property as been set
        setVisible(true);
    }

    private ScaleTransition createScaleTransition() {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(duration), this);
        scaleTransition.setByX(1f * 3 / 2);
        scaleTransition.setByY(1f * 3 / 2);
        scaleTransition.setCycleCount(nb);
        scaleTransition.setAutoReverse(true);
        return scaleTransition;
    }

}
