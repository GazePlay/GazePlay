package net.gazeplay.games.training;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;

public class EventItemEmmanuel extends Rectangle{
    private final javafx.event.EventHandler<ActionEvent> event;
    public final boolean remove;
    private boolean isActivable;

    EventItemEmmanuel(double x, double y, double width, double height, ImagePattern im, javafx.event.EventHandler<ActionEvent> event, boolean remove){
        super(x,y,width,height);
        this.setFill(im);
        this.event = event;
        this.remove = remove;
        isActivable = true;
    }

    public void active(){
        if (isActivable) {
            isActivable = false;
            PauseTransition wait = new PauseTransition(Duration.millis(10));
            wait.setOnFinished(event);
            wait.play();
            doActivable();
        }
    }

    private void doActivable(){
        PauseTransition wait = new PauseTransition(Duration.millis(2000));
        wait.setOnFinished(e -> isActivable = true);
        wait.play();
    }
}
