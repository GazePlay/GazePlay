package net.gazeplay.games.shooter;

import javafx.animation.Transition;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;

public class Target extends StackPane {

    @Getter
    @Setter
    private Point destination;

    @Getter
    @Setter
    private Transition transition;

    @Getter
    @Setter
    private double centerX;

    @Getter
    @Setter
    private double centerY;

    @Getter
    @Setter
    private boolean done = false;

    @Getter
    @Setter
    private boolean animDone = true;

}
