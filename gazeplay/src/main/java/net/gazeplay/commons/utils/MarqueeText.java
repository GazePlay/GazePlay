package net.gazeplay.commons.utils;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.Utils;

/**
 * This object is supposed to have an object which will have a text whil will scroll
 * horizontally to give a marquee effect. I didn't manage to get something 
 * satisfying in general (espacially with text position and text disparition).
 * However this is suffecient for the volume control panel.
 */
@Slf4j
public class MarqueeText extends Region {


    private final TranslateTransition transition;

    private final Text text;
    
    @Getter
    private final IntegerProperty nbCharDisplayed = new SimpleIntegerProperty(this, "nbCharDisplayed", 8);
    
    @Getter
    private final StringProperty textProperty = new SimpleStringProperty(this, "textProperty", "");

    @Getter
    private final DoubleProperty speed = new SimpleDoubleProperty(this, "speed", 0);
    
    private final static double DEFAULT_SPEED = 40;
    
    private final static int DEFAULT_NB_CHAR_DISPLAYED = 25;

    public MarqueeText(final String text, final int nbCharDisplayed, final double speed) {
        super();
        this.nbCharDisplayed.setValue(nbCharDisplayed);
        this.speed.setValue(speed);

        this.text = new Text(text);
        this.getChildren().add(this.text);

        transition = TranslateTransitionBuilder.create().duration(new Duration(speed)).node(this.text)
                .interpolator(Interpolator.LINEAR).cycleCount(Animation.INDEFINITE).build();
        transition.setAutoReverse(true);

        transition.setOnFinished((ActionEvent actionEvent) -> {
            rerunAnimation();
        });
        
        this.getTextProperty().addListener((observable) -> {
            this.text.setText(this.getTextProperty().getValue());
            log.info("new text : {}", this.text);
            rerunAnimation();
        });

        this.nbCharDisplayed.addListener((observable) -> {
            rerunAnimation();
        });

        this.speed.addListener((observable) -> {
            rerunAnimation();
        });

        this.setBorder(new Border(new BorderStroke(Color.BLACK, 
            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        
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

        log.info("duration : {}", transition.getDuration().toMillis());
        if (transition.getDuration().toMillis() > 0) {
            transition.playFromStart();
        }
    }

    private void recalculateTransition() {
        
        this.text.setX(this.getTranslateX());
        this.text.setY(this.getTranslateY() + 15);
        final double textWidth = getTextWidth(text.getText());
        
        int lastIndex = text.getText().length();
        if(lastIndex > nbCharDisplayed.getValue()) {
            lastIndex = nbCharDisplayed.getValue();
        }
        final double maxTextWidth = getTextWidth(text.getText().substring(0, lastIndex));

        double diff = textWidth - maxTextWidth;
        log.info("textWidth {}, maxTextWidth {}, diff {}", textWidth, maxTextWidth, diff);
        if (diff < 0) {
            transition.setDuration(new Duration(0));
        } else {
            transition.setToX(this.getBoundsInLocal().getMinX() - diff);
            transition.setFromX(this.getBoundsInLocal().getMinX());
            transition.setDuration(new Duration(computeDuration() * 1000));
        }
        
        this.setPrefWidth(maxTextWidth);
        this.setMaxWidth(maxTextWidth);
        log.info("this width {}", this.getWidth());
    }
    
    private double computeDuration() {
        
        final double textWidth = getTextWidth(text.getText());
        log.info ("textWidth : {} / speed : {}", textWidth, speed.getValue());
        return textWidth / speed.getValue();
    }
    
    private double getTextWidth(final String textToMeasure) {

        final Text testText = new Text(textToMeasure);
        // java 7 =>
        // text.snapshot(null, null);
        // java 8 =>
        
        testText.setFont(this.text.getFont());
        testText.applyCss();
        double width = testText.getBoundsInLocal().getWidth();

        return width;
    }
}
