package net.gazeplay.games.mediaPlayer;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.io.File;

public class MediaButton extends Button {

    @Getter
    @Setter
    private EventHandler<Event> mediaEvent = e -> { };

    @Getter
    @Setter
    private MediaFile mediaFile;

    MediaButton(double width, double height){
        super();
        this.setPrefWidth(width);
        this.setPrefHeight(height);
    }

    public void setupImage(){
        if (mediaFile != null && mediaFile.getImagepath() != null) {
            final File f = new File(mediaFile.getImagepath());
            final ImageView iv = new ImageView(new Image(f.toURI().toString()));
            iv.setPreserveRatio(true);
            iv.fitHeightProperty().bind(this.heightProperty().multiply(9.0 / 10.0));
            iv.fitWidthProperty().bind(this.widthProperty().multiply(9.0 / 10.0));
            this.setGraphic(iv);
        } else {
            this.setGraphic(null);
        }
    }

    public void setupEvent(EventHandler<Event> newMediaEvent){
        this.removeEventFilter(MouseEvent.MOUSE_CLICKED, mediaEvent);
        this.removeEventFilter(GazeEvent.GAZE_ENTERED, mediaEvent);
       this.setMediaEvent(newMediaEvent);
       this.addEventFilter(MouseEvent.MOUSE_CLICKED, this.getMediaEvent());
       this.addEventFilter(GazeEvent.GAZE_ENTERED, this.getMediaEvent());
    }


}
