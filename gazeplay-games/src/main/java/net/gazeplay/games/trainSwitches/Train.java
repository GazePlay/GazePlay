package net.gazeplay.games.trainSwitches;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Train {

    private final String color;
    private final ImageView shape;

    public Train(String color, String direction) {
        this.color = color;
        shape = new ImageView(new Image("data/trainSwitches/images/"+color+"Loco.png"));
        shape.setPreserveRatio(true);
        shape.setMouseTransparent(true);
        switch (direction){
            case "up":
                shape.setRotate(90);
                break;
            case "down":
                shape.setRotate(-90);
                break;
            case "right":
                shape.setScaleX(-1);
                break;
            default:
                break;
        }
    }

    public String getColor() {
        return color;
    }

    public ImageView getShape() {
        return shape;
    }

}
