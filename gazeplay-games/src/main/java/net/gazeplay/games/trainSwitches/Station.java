package net.gazeplay.games.trainSwitches;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Station {

    private Color color;
    private Rectangle shape;

    public Station(Color color) {
        this.color = color;
        this.shape = new Rectangle();
        this.shape.setWidth(100);
        this.shape.setHeight(100);
        this.shape.setFill(color);
    }

    public Color getColor() {
        return color;
    }

    public Rectangle getShape() {
        return shape;
    }

}
