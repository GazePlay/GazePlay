package net.gazeplay.games.biboules;

import javafx.animation.Transition;
import javafx.scene.layout.StackPane;

public class Target extends StackPane {

    Boolean explode;
    Point destination;
    Transition t;

    Target() {
        super();
        explode = false;

    }

}
