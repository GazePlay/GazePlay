package net.gazeplay.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

/**
 * A pane selected by a fixation of 2 seconds (fixed, not the length of the settings) of the gaze or of the mouse, shown
 * by a progress indicator. It used to react to the gaze only: with the mouse alone, the dice of the Dice game could not
 * be rolled one by one.
 */
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

    /**
     * Enables the selection if {@code b}, else disables it.
     */
    public void enable(final boolean b) {
        if (b) {
            enable();
        } else {
            disable();
        }
    }

    /**
     * Stops listening to the gaze and the mouse.
     */
    public void disable() {
        this.removeEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        this.removeEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
        this.removeEventFilter(MouseEvent.MOUSE_EXITED, exitbuttonHandler);
        this.removeEventFilter(MouseEvent.MOUSE_ENTERED, enterbuttonHandler);
    }

    /**
     * Listens to the gaze and the mouse again.
     */
    public void enable() {
        this.addEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitbuttonHandler);
        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterbuttonHandler);
    }

    /**
     * Creates the progress indicator, which follows the size and position of the pane.
     */
    public void init() {
        buttonWidth = 0;
        buttonHeight = 0;
        indicator = new ProgressIndicator(0);
        button.heightProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinHeight(newVal.doubleValue() * 0.9);
            buttonHeight = newVal.doubleValue();
            indicator.setTranslateY(
                indicator.getTranslateY() - (oldVal.doubleValue() / 0.1) + (newVal.doubleValue()) * 0.1);
            indicator.toFront();
        });
        button.widthProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinWidth(newVal.doubleValue() * 0.9);
            buttonWidth = newVal.doubleValue();
            indicator.setTranslateX(
                indicator.getTranslateX() - (oldVal.doubleValue() / 0.1) * 2 + (newVal.doubleValue() / 2) * 0.1);
            indicator.toFront();
        });
        button.layoutXProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setTranslateX(newVal.doubleValue() + (buttonWidth / 2) * 0.1);
            indicator.toFront();
        });
        button.layoutYProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setTranslateY(newVal.doubleValue() + buttonHeight * 0.1);
            indicator.toFront();
        });
    }

    /**
     * Sets the action run after a fixation of 2 seconds on the pane.
     */
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
        exitbuttonHandler = e -> {
            if (timelineProgressBar != null) {
                timelineProgressBar.stop();
            }
            indicator.setOpacity(0);
            indicator.setProgress(0);

        };
        enable();
    }

}
