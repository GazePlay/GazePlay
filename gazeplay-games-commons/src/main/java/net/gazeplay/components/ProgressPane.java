package net.gazeplay.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Slf4j
public class ProgressPane extends StackPane {

    public final BorderPane button;
    ProgressIndicator indicator;
    Timeline timelineProgressBar;
    double buttonWidth;
    double buttonHeight;
    EventHandler<Event> enterbuttonHandler;
    EventHandler<Event> exitbuttonHandler;

    public ProgressPane() {
        super();
        button = new BorderPane();
        init();
        this.getChildren().addAll(button, indicator);
    }

    public void enable(final boolean b) {
        if (b) {
            enable();
        } else {
            disable();
        }
    }

    public void disable() {
        this.removeEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        this.removeEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
    }

    public void enable() {
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
    }

    public void init() {
        buttonWidth = 0;
        buttonHeight = 0;
        indicator = new ProgressIndicator(0);
        button.heightProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinHeight(newVal.doubleValue() * 0.9);
            buttonHeight = newVal.doubleValue();
            indicator.setTranslateY(
                indicator.getTranslateY() - (oldVal.doubleValue() / 0.1) + (newVal.doubleValue()) * 0.1);
            // log.info("button size modified: " + newVal.doubleValue());
            indicator.toFront();
        });
        button.widthProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinWidth(newVal.doubleValue() * 0.9);
            buttonWidth = newVal.doubleValue();
            indicator.setTranslateX(
                indicator.getTranslateX() - (oldVal.doubleValue() / 0.1) * 2 + (newVal.doubleValue() / 2) * 0.1);
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
    }

    public void assignIndicator(final EventHandler<Event> enterEvent) {
        indicator.setMouseTransparent(true);

        indicator.setOpacity(0);
        enterbuttonHandler = e -> {
            indicator.toFront();
            indicator.setOpacity(1);
            timelineProgressBar = new Timeline();

            timelineProgressBar.getKeyFrames()
                .add(new KeyFrame(new Duration(2000), new KeyValue(indicator.progressProperty(), 1)));

            timelineProgressBar.onFinishedProperty().set(actionEvent -> {
                enterEvent.handle(null);
                exitbuttonHandler.handle(null);

            });
            timelineProgressBar.play();

        };
        // this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterbuttonHandler);
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);

        exitbuttonHandler = e -> {
            timelineProgressBar.stop();
            indicator.setOpacity(0);
            indicator.setProgress(0);

        };
        // this.addEventFilter(MouseEvent.MOUSE_EXITED, exitbuttonHandler);
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);

    }

}
