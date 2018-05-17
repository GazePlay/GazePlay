package net.gazeplay.commons.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

public class ProgressButton extends Button {

    ProgressIndicator indicator;
    Timeline timelineProgressBar;

    public ProgressButton() {
        super();
    }

    public ProgressButton(String s) {
        super(s);
    }

    public ProgressIndicator assignIndicator(double buttonSize, EventHandler<Event> enterEvent) {
        indicator = new ProgressIndicator(0);
        indicator.setTranslateX(this.getLayoutX() + (buttonSize / 2) * 0.1);
        indicator.setTranslateY(this.getLayoutY() + buttonSize * 0.1);
        indicator.setMinWidth(buttonSize * 0.9);
        indicator.setMinHeight(buttonSize * 0.9);
        indicator.setMouseTransparent(true);

        indicator.setOpacity(0);
        EventHandler<Event> enterbuttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                indicator.setOpacity(1);
                timelineProgressBar = new Timeline();

                timelineProgressBar.getKeyFrames()
                        .add(new KeyFrame(new Duration(500), new KeyValue(indicator.progressProperty(), 1)));

                timelineProgressBar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        enterEvent.handle(null);
                    }
                });
                timelineProgressBar.play();

            }
        };
        // this.addEventHandler(MouseEvent.MOUSE_ENTERED, enterbuttonHandler);
        this.addEventHandler(GazeEvent.GAZE_ENTERED, enterbuttonHandler);

        EventHandler<Event> exitbuttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                timelineProgressBar.stop();
                indicator.setOpacity(0);
                indicator.setProgress(0);

            }
        };
        // this.addEventHandler(MouseEvent.MOUSE_EXITED, exitbuttonHandler);
        this.addEventHandler(GazeEvent.GAZE_EXITED, exitbuttonHandler);
        return indicator;
    }

}
