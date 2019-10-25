package net.gazeplay;

import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import lombok.Data;
import lombok.NonNull;

@Data
public class GameButtonPane extends BorderPane {

    @NonNull
    private final GameSpec gameSpec;

    private EventHandler<Event> eventhandler;

    private EventHandler<Event> enterhandler;

    private EventHandler<Event> exithandler;

    private Timeline timelineProgressBar;

    private boolean active = false;

}
