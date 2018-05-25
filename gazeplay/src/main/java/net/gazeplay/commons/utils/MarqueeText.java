package net.gazeplay.commons.utils;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.Utils;

@Slf4j
public class MarqueeText extends StackPane {

    @Getter
    private final IntegerProperty nbCharDisplayed = new SimpleIntegerProperty(this, "nbCharDisplayed", 8);

    private final TranslateTransition transition;

    private final Label text;
    
    @Getter
    private final StringProperty textProperty = new SimpleStringProperty(this, "textProperty", "");
    

    @Getter
    private final ReadOnlyDoubleProperty duration = new ReadOnlyDoubleWrapper(this, "duration", 0);

    @Getter
    private final DoubleProperty desiredDuration = new SimpleDoubleProperty(this, "desieredDuration", 0);

    public MarqueeText(final int nbCharDisplayed, final String text, final double duration) {
        super();
        this.nbCharDisplayed.setValue(nbCharDisplayed);
        this.desiredDuration.setValue(duration);

        this.text = new Label(text);
        this.getChildren().add(this.text);

        transition = TranslateTransitionBuilder.create().duration(new Duration(duration)).node(this.text)
                .interpolator(Interpolator.LINEAR).cycleCount(1).build();

        transition.setOnFinished((ActionEvent actionEvent) -> {
            rerunAnimation();
        });

        /*this.textProperty().addListener((observable) -> {
            if(!getText().equals("")) {
                this.text.setText(this.getText());
                this.setText("");
                log.info("new text : {}", this.text);
                rerunAnimation();
            }
        });*/
        
        this.getTextProperty().addListener((observable) -> {
            this.text.setText(this.getTextProperty().getValue());
            log.info("new text : {}", this.text);
            rerunAnimation();
        });

        this.nbCharDisplayed.addListener((observable) -> {
            rerunAnimation();
        });

        this.duration.addListener((observable) -> {
            rerunAnimation();
        });

        rerunAnimation();
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
        /*
         * transition.setToX(this.getBoundsInLocal().getMaxX() * -1 - 100);
         * transition.setFromX(parentPane.widthProperty().get() + 100);
         * 
         * double distance = parentPane.widthProperty().get() + 2 * node.getBoundsInLocal().getMaxX();
         * transition.setDuration(new Duration(distance / SPEED_FACTOR));
         */

        final double textWidth = Utils.getTextWidth(text.getText());

        final String testText = new String(new char[this.nbCharDisplayed.getValue()]).replace('\0', '-');
        final double maxTextWidth = Utils.getTextWidth(testText);

        // TODO : rework transition
        /*transition.setToX(this.getTranslateX() + textWidth);
        transition.setFromX(this.getTranslateX());*/

        log.info("text width : {}, max textWidth {}", textWidth, maxTextWidth);
        if (textWidth <= maxTextWidth) {
            transition.setDuration(new Duration(0));
        } else {
            transition.setDuration(new Duration(desiredDuration.getValue()));
        }

        this.text.setPrefWidth(maxTextWidth);
        this.text.setMaxWidth(maxTextWidth);
        
        log.info("width : {}", this.getWidth());
    }
}
