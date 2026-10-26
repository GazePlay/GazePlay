package net.gazeplay.games.follow;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * An item of the Follow game (coin, key, door...) acting when the player reaches it; it can act again after 2 seconds,
 * unless it is removed.
 */
public class EventItem extends Rectangle{
    private final javafx.event.EventHandler<ActionEvent> event;
    public final boolean remove;
    private boolean isActivable;

    EventItem(double x, double y, double width, double height, ImagePattern im, javafx.event.EventHandler<ActionEvent> event, boolean remove){
        super(x,y,width,height);
        this.setFill(im);
        this.event = event;
        this.remove = remove;
        isActivable = true;
    }

    /**
     * Runs the action of the item, if it can act.
     */
    public void active(){
        if (isActivable) {
            isActivable = false;
            PauseTransition wait = new PauseTransition(Duration.millis(10));
            wait.setOnFinished(event);
            wait.play();
            doActivable();
        }
    }

    /**
     * Lets the item act again after 2 seconds.
     */
    private void doActivable(){
        PauseTransition wait = new PauseTransition(Duration.millis(2000));
        wait.setOnFinished(e -> isActivable = true);
        wait.play();
    }
}
