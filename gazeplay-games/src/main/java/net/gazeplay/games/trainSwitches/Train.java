package net.gazeplay.games.trainSwitches;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Train {

    private Color color;
    private Circle shape;

    public Train(Color color) {
        this.color = color;
        shape = new Circle(50);
        shape.setFill(color);
        shape.setMouseTransparent(true);
    }

    public Color getColor() {
        return color;
    }

    public Circle getShape() {
        return shape;
    }

}
