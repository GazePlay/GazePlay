package net.gazeplay.games.shooter;

import javafx.animation.Transition;
import javafx.scene.layout.StackPane;
import lombok.Data;

@Data
public class Target extends StackPane {

    private Point destination;
    private Transition transition;
    private double centerX;
    private double centerY;
    private boolean done;
    private boolean animDone;

    Target() {
        super();
        animDone = true;
        done = false;
    }

}
