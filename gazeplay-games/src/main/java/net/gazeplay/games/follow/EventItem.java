package net.gazeplay.games.follow;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class EventItem{
    public Rectangle rectangle;
    private final javafx.event.EventHandler<ActionEvent> event;

    EventItem(double x, double y, double width, double height, ImagePattern Im, javafx.event.EventHandler<ActionEvent> event){
        rectangle = new Rectangle(x, y, width, height);
        rectangle.setFill(Im);
        this.event = event;
    }

    public void active(){
        PauseTransition Wait = new PauseTransition(Duration.millis(5));
        Wait.setOnFinished(event);
        Wait.play();
    }
}
