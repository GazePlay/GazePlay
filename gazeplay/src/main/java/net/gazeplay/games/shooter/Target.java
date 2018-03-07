package net.gazeplay.games.shooter;

import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.scene.layout.StackPane;
import net.gazeplay.games.shooter.Point;

public class Target extends StackPane {

    Point destination;
    Transition t;
    double centerX, centerY;
    Boolean done;
    Boolean animDone;

    Target() {
        super();
        animDone = true;
        done = false;

    }

}
