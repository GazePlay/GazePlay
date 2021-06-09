package net.gazeplay.games.follow;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class EventItem{
    public Rectangle rectangle;
    private final javafx.event.EventHandler<ActionEvent> event;
    public final boolean remove;
    public boolean multigoals;

    EventItem(double x, double y, double width, double height, ImagePattern Im, javafx.event.EventHandler<ActionEvent> event, boolean remove){
        rectangle = new Rectangle(x, y, width, height);
        rectangle.setFill(Im);
        this.event = event;
        this.remove = remove;
        multigoals = true;
    }

    EventItem(double x, double y, double width, double height, ImagePattern Im, javafx.event.EventHandler<ActionEvent> event, boolean remove, boolean multigoals){
        rectangle = new Rectangle(x, y, width, height);
        rectangle.setFill(Im);
        this.event = event;
        this.remove = remove;
        this.multigoals = multigoals;
    }

    public void active(){
        multigoals = true;
        PauseTransition Wait = new PauseTransition(Duration.millis(10));
        Wait.setOnFinished(event);
        Wait.play();
    }
}
