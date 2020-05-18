package net.gazeplay.commons.utils;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This object is supposed to have a text whil will scroll horizontally to give a marquee effect if the text is larger
 * than its width.
 */
@Slf4j
public class MarqueeText extends Region {

    private final TranslateTransition transition;

    private final Text text;

    @Getter
    private final DoubleProperty speed = new SimpleDoubleProperty(this, "speed", 0);

    @Getter
    private final StringProperty textProperty = new SimpleStringProperty(this, "text", "");

    private final static double DEFAULT_SPEED = 40;

    private final static int DEFAULT_NB_CHAR_DISPLAYED = 25;

    public MarqueeText(final String text, final int nbCharDisplayed, final double speed) {
        super();
        this.speed.setValue(speed);

        this.text = new Text(text);
        this.getChildren().add(this.text);

        // As of OpenJFX 9, all builders are deprecated. I have therefore matched the previous builder (see Git
        // commit history) with a standard constructor and setters.
        transition = new TranslateTransition(new Duration(speed), this.text);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.setAutoReverse(true);

        transition.setOnFinished((ActionEvent actionEvent) -> rerunAnimation());

        this.getTextProperty().addListener((observable) -> {
            this.text.setText(this.getTextProperty().getValue());
            log.info("new text : {}", this.text);
            rerunAnimation();
        });

        this.widthProperty().addListener((observable) -> rerunAnimation());

        this.speed.addListener((observable) -> rerunAnimation());

        rerunAnimation();
    }

    public MarqueeText(final String text, final int nbCharDisplayed) {
        this(text, nbCharDisplayed, DEFAULT_SPEED);
    }

    public MarqueeText(final String text) {
        this(text, DEFAULT_NB_CHAR_DISPLAYED, DEFAULT_SPEED);
    }

    private void rerunAnimation() {
        transition.stop();
        recalculateTransition();

        // log.info("duration : {}", transition.getDuration().toMillis());
        if (transition.getDuration().toMillis() > 0) {
            transition.playFromStart();
        }
    }

    private void recalculateTransition() {

        this.text.setTranslateX(this.getTranslateX());
        this.text.setTranslateY(this.getTranslateY() + 15);
        final double textWidth = getTextWidth(text.getText());

        final double diff = textWidth - this.getWidth();
        // log.info("textWidth {}, diff {}", textWidth, diff);
        if (diff < 0) {
            transition.setDuration(new Duration(0));
        } else {
            transition.setToX(this.getBoundsInLocal().getMinX() - diff);
            transition.setFromX(this.getBoundsInLocal().getMinX());
            transition.setDuration(new Duration(computeDuration()));
        }
    }

    private double computeDuration() {

        final double textWidth = getTextWidth(text.getText());
        // log.info ("textWidth : {} / speed : {} = duration {}", textWidth, speed.getValue(), duration);
        return textWidth / speed.getValue() * 1000;
    }

    private double getTextWidth(final String textToMeasure) {

        final Text testText = new Text(textToMeasure);

        testText.setFont(this.text.getFont());
        testText.applyCss();

        return testText.getBoundsInLocal().getWidth();
    }
}
