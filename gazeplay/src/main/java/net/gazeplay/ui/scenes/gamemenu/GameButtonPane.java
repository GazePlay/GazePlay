package net.gazeplay.ui.scenes.gamemenu;

import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.gazeplay.GameSpec;

@RequiredArgsConstructor
public class GameButtonPane extends BorderPane {

    @Getter
    @NonNull
    private final GameSpec gameSpec;

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

}
