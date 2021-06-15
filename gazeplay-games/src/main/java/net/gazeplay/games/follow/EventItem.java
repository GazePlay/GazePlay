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
    private boolean isActivable;

    EventItem(double x, double y, double width, double height, ImagePattern Im, javafx.event.EventHandler<ActionEvent> event, boolean remove){
        rectangle = new Rectangle(x, y, width, height);
        rectangle.setFill(Im);
        this.event = event;
        this.remove = remove;
        isActivable = true;
    }

    public void active(){
        if (isActivable) {
            isActivable = false;
            PauseTransition Wait = new PauseTransition(Duration.millis(10));
            Wait.setOnFinished(event);
            Wait.play();
            doActivable();
        }
    }

    private void doActivable(){
        PauseTransition Wait = new PauseTransition(Duration.millis(2000));
        Wait.setOnFinished(e -> isActivable = true);
        Wait.play();
    }
}
