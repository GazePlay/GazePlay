package net.gazeplay.games.race;

import javafx.animation.Transition;
import javafx.scene.layout.StackPane;

/**
 * A bug or a racer of the FrogsRace game.
 */
public class Target extends StackPane {

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
