package net.gazeplay.games.trainSwitches;

import javafx.geometry.Point2D;
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

    public Point2D getCenter() {
        return new Point2D(shape.getX() + shape.getWidth()/2,shape.getY() + shape.getHeight()/2);
    }

    public boolean isInside(double x, double y){
        return Math.abs(x - (shape.getX() + shape.getWidth()/2))<=10 && Math.abs(y - (shape.getY() + shape.getHeight()/2))<=10;
    }

}
