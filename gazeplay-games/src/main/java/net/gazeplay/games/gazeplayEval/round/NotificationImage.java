package net.gazeplay.games.gazeplayEval.round;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class NotificationImage extends Rectangle {
    public NotificationImage(Image image, double initialX, double initialY, double width, double height, double translateX, double translateY) {
        super(width, height);
        this.setFill(new ImagePattern(image));
        this.setX(initialX);
        this.setY(initialY);
        this.setTranslateX(translateX);
        this.setTranslateY(translateY);
        this.setOpacity(0);
        this.setVisible(false);
    }
}
