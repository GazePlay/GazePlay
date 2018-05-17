package net.gazeplay.games.cakes;

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
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class progessButton extends Button {

    ProgressIndicator indicator;
    Timeline timelineProgressBar;

    public ProgressIndicator assignIndicator(ProgressIndicator pi, double buttonSize) {
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

                timelineProgressBar.play();

            }
        };
        this.addEventHandler(MouseEvent.MOUSE_ENTERED, enterbuttonHandler);

        EventHandler<Event> exitbuttonHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                timelineProgressBar.stop();
                indicator.setOpacity(0);
                indicator.setProgress(0);

            }
        };
        this.addEventHandler(MouseEvent.MOUSE_EXITED, exitbuttonHandler);
        return indicator;
    }

}
