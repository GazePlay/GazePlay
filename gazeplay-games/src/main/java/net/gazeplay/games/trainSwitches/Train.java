package net.gazeplay.games.trainSwitches;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.util.ArrayList;

public class Train {

    private final String color;
    private final ImageView shape;
    private final ArrayList<Image> images;
    private int index;

    public Train(TrainColors trainColor, String direction) {
        this.color = trainColor.getColor();

        // Create a small animation for the train consisting of 2 images
        index = 0;
        images = new ArrayList<>();
        images.add(new Image("data/trainSwitches/images/"+color+"Loco.png"));
        images.add(new Image("data/trainSwitches/images/"+color+"Loco2.png"));
        shape = new ImageView(images.get(index));
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300), actionEvent -> shape.setImage(images.get(index++%images.size()))));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        shape.setPreserveRatio(true);
        shape.setMouseTransparent(true);
        switch (direction) {
            case "up" -> shape.setRotate(90);
            case "down" -> shape.setRotate(-90);
            case "right" -> shape.setScaleX(-1);
            case "left" -> {
                shape.setScaleX(-1);
                shape.setScaleY(-1);
            }
            default -> {
            }
        }
    }

    public String getColor() {
        return color;
    }

    public ImageView getShape() {
        return shape;
    }

}
