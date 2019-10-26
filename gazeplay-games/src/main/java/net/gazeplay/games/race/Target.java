package net.gazeplay.games.race;

import javafx.animation.Transition;
import javafx.scene.layout.StackPane;

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
