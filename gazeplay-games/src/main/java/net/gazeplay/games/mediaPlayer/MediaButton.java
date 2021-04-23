package net.gazeplay.games.mediaPlayer;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.components.GazeIndicator;
import net.gazeplay.components.StackPaneButton;

import java.io.File;

public class MediaButton extends StackPaneButton {

    public static final EventHandler<Event> emptyEvent = e -> {
    };

    @Getter
    private EventHandler<Event> enterEvent = emptyEvent;
    @Getter
    private EventHandler<Event> exitEvent = emptyEvent;
    @Getter
    private EventHandler<Event> clickEvent = emptyEvent;

    @Getter
    @Setter
    private MediaFile mediaFile;

    MediaButton(double width, double height) {
        super();
        this.getButton().setPrefWidth(width);
        this.getButton().setPrefHeight(height);
    }

    void setupImage() {
        if (mediaFile != null && mediaFile.getImagepath() != null) {
            final File f = new File(mediaFile.getImagepath());
            final ImageView iv = new ImageView(new Image(f.toURI().toString()));
            iv.setPreserveRatio(true);
            iv.fitHeightProperty().bind(this.getButton().heightProperty().multiply(9.0 / 10.0));
            iv.fitWidthProperty().bind(this.getButton().widthProperty().multiply(9.0 / 10.0));
            this.getButton().setGraphic(iv);
        } else {
            this.getButton().setGraphic(null);
        }
    }

    void setupEvent(EventHandler<ActionEvent> newMediaEvent, GazeIndicator progressIndicator) {
        this.removeEventFilter(MouseEvent.MOUSE_CLICKED, clickEvent);
        this.removeEventFilter(TouchEvent.TOUCH_RELEASED, clickEvent);
        this.removeEventFilter(GazeEvent.GAZE_ENTERED, enterEvent);
        this.removeEventFilter(GazeEvent.GAZE_EXITED, exitEvent);

        clickEvent = e -> {
            newMediaEvent.handle(null);
        };

        this.addEventFilter(MouseEvent.MOUSE_CLICKED, clickEvent);
        this.addEventFilter(TouchEvent.TOUCH_RELEASED, clickEvent);

        enterEvent = eventEntered -> {
            if(!this.getChildren().contains(progressIndicator)) {
                this.getChildren().add(progressIndicator);
            }
            progressIndicator.setOnFinish(newMediaEvent);
            progressIndicator.start();
        };

        exitEvent = eventExited -> {
            progressIndicator.stop();
            this.getChildren().remove(progressIndicator);
        };

        this.addEventFilter(MouseEvent.MOUSE_ENTERED, enterEvent);

        this.addEventFilter(MouseEvent.MOUSE_EXITED, exitEvent);

        this.addEventFilter(GazeEvent.GAZE_ENTERED, enterEvent);

        this.addEventFilter(GazeEvent.GAZE_EXITED, exitEvent);
    }


}
