package net.gazeplay;

import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.Setter;

public class GameButtonPane extends BorderPane {

    @Getter
    @Setter
    private EventHandler<Event> eventhandler;

    @Getter
    @Setter
    private EventHandler<Event> enterhandler;

    @Getter
    @Setter
    private EventHandler<Event> exithandler;

    @Getter
    @Setter
    private Timeline timelineProgressBar;

    @Getter
    @Setter
    private boolean active = false;

}
