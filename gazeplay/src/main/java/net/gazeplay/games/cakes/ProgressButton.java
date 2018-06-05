package net.gazeplay.games.cakes;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class ProgressButton extends StackPane {

    public Button button;
    ProgressIndicator indicator;
    Timeline timelineProgressBar;
    double buttonWidth;
    double buttonHeight;
    EventHandler<Event> enterbuttonHandler;
    EventHandler<Event> exitbuttonHandler;

    public ProgressButton() {
        super();
        button = new Button();
        init();
        this.getChildren().addAll(button, indicator);
    }

    public void disable() {
        this.removeEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
        this.removeEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
    }

    public void active() {
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
    }

    public void init() {
        buttonWidth = 0;
        buttonHeight = 0;
        indicator = new ProgressIndicator(0);
        indicator.setMouseTransparent(true);
        button.heightProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinHeight(newVal.doubleValue() * 0.9);
            buttonHeight = newVal.doubleValue();
            // indicator.setTranslateY(indicator.getTranslateY()-(oldVal.doubleValue()/0.1) +
            // (newVal.doubleValue())*0.1);
            // log.info("button size modified: " + newVal.doubleValue());
            indicator.toFront();
        });
        button.widthProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinWidth(newVal.doubleValue() * 0.9);
            buttonWidth = newVal.doubleValue();
            // indicator.setTranslateX(indicator.getTranslateX()-(oldVal.doubleValue()/0.1)*2 +
            // (newVal.doubleValue()/2)*0.1);
            indicator.toFront();
            // log.info("button size modified: " + newVal.doubleValue());
        });
        button.layoutXProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setTranslateX(newVal.doubleValue() + (buttonWidth / 2) * 0.1);
            indicator.toFront();
            // log.info("position changed: " + newVal.doubleValue());
        });
        button.layoutYProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setTranslateY(newVal.doubleValue() + buttonHeight * 0.1);
            indicator.toFront();
            // log.info("position changed: " + newVal.doubleValue());
        });

        indicator.setOpacity(0);
    }

    public ProgressIndicator assignIndicator(EventHandler<Event> enterEvent) {
        indicator.setMouseTransparent(true);
        indicator.setOpacity(0);
        ProgressButton pb = this;
        Event e1 = new Event(pb, pb, GazeEvent.ANY);

        enterbuttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                indicator.setOpacity(1);
                timelineProgressBar = new Timeline();

                timelineProgressBar.setDelay(new Duration(500));

                timelineProgressBar.getKeyFrames()
                        .add(new KeyFrame(new Duration(500), new KeyValue(indicator.progressProperty(), 1)));

                timelineProgressBar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        indicator.setOpacity(0);
                        if (enterEvent != null) {
                            enterEvent.handle(e1);
                        }
                    }
                });
                timelineProgressBar.play();

            }
        };

        exitbuttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                timelineProgressBar.stop();
                indicator.setOpacity(0);
                indicator.setProgress(0);

            }
        };

        return indicator;
    }

}
