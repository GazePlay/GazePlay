package net.gazeplay.commons.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

@Data
@Slf4j
public class ProgressButton extends StackPane {

    public Circle button;
    ProgressIndicator indicator;
    Timeline timelineProgressBar;
    double buttonWidth;
    double buttonHeight;
    EventHandler<Event> enterbuttonHandler;
    EventHandler<Event> exitbuttonHandler;
    boolean inuse = false;

    ImageView image;

    public ProgressButton() {
        super();
        button = new Circle();
        button.setFill(Color.LIGHTGREY);
        init();
        image = new ImageView();
        this.getChildren().addAll(button, image, indicator);
    }

    public void active() {
        inuse = true;
        this.setOpacity(1);
        this.indicator.setOpacity(0);
    }

    public void disable() {
        inuse = false;
        this.setOpacity(0);
    }

    public void setImage(ImageView img) {
        image = img;
        image.setFocusTraversable(true);
        image.setMouseTransparent(true);
        this.getChildren().set(1, image);
    }

    public void disable2() {
        this.removeEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
        this.removeEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        this.setDisable(true);
        this.setOpacity(0);
    }

    public void active2() {
        this.button.addEventFilter(GazeEvent.GAZE_ENTERED, enterbuttonHandler);
        this.button.addEventFilter(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        this.setDisable(false);
        this.setOpacity(1);
        this.indicator.setOpacity(0);

        this.button.addEventFilter(MouseEvent.MOUSE_ENTERED, enterbuttonHandler);
        this.button.addEventFilter(MouseEvent.MOUSE_EXITED, exitbuttonHandler);
    }

    public void init() {
        buttonWidth = 0;
        buttonHeight = 0;
        indicator = new ProgressIndicator(0);
        indicator.setMouseTransparent(true);
        button.radiusProperty().addListener((obs, oldVal, newVal) -> {
            indicator.setMinHeight(2 * newVal.doubleValue());
            indicator.setMinWidth(2 * newVal.doubleValue());
            buttonHeight = newVal.doubleValue();
            double width = newVal.doubleValue() * 2;
            width = (width * 90) / 100;
            image.setFitWidth(width);
            indicator.toFront();
        });

        indicator.setOpacity(0);
    }

    public ProgressIndicator assignIndicator(EventHandler<Event> enterEvent, int fixationLength) {
        indicator.setMouseTransparent(true);
        indicator.setOpacity(0);
        ProgressButton pb = this;
        Event e1 = new Event(pb, pb, GazeEvent.ANY);

        enterbuttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (inuse) {
                    image.toFront();
                    indicator.setProgress(0);
                    indicator.setOpacity(1);
                    timelineProgressBar = new Timeline();

                    timelineProgressBar.setDelay(new Duration(500));

                    timelineProgressBar.getKeyFrames().add(
                            new KeyFrame(new Duration(fixationLength), new KeyValue(indicator.progressProperty(), 1)));

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
            }
        };

        exitbuttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (inuse) {
                    timelineProgressBar.stop();
                    indicator.setOpacity(0);
                    indicator.setProgress(0);
                }
            }
        };

        active2();

        return indicator;
    }

}
