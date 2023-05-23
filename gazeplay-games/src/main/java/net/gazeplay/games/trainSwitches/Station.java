package net.gazeplay.games.trainSwitches;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Station {

    private final String color;
    private final ImageView shape;

    public Station(String color) {
        this.color = color;
        shape = new ImageView(new Image("data/trainSwitches/images/"+color+"Station.png"));
    }

    public String getColor() {
        return color;
    }

    public ImageView getShape() {
        return shape;
    }

    public boolean isInside(double x, double y){
        return Math.abs(x - (shape.getX() + shape.getFitWidth()/2))<=50 && Math.abs(y - (shape.getY() + shape.getFitHeight()/2))<=50;
    }

}
