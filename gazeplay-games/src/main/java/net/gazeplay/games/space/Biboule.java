package net.gazeplay.games.space;

import javafx.animation.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Biboule extends Rectangle {
    @Getter
    private String soundFileLocation;
    @Getter
    private Rectangle collider;
    private Timeline leftMovement;
    private Timeline rightMovement;
    private double animTime;

    public Biboule(double x, double y, double width, double height, String soundFileLocation, double windowWidth,
            double speed) {
        this(x, y, width, height, soundFileLocation, windowWidth, speed, 0, 0, 0, 0);
    }

    public Biboule(double x, double y, double width, double height, String soundFileLocation, double windowWidth,
            double speed, double colliderMarginUp, double colliderMarginRight, double colliderMarginDown,
            double colliderMarginLeft) {
        super(x, y, width, height);
        this.soundFileLocation = soundFileLocation;
        this.collider = new Rectangle(x + colliderMarginLeft * width, y + colliderMarginUp * height,
                width * (1 - colliderMarginLeft - colliderMarginRight),
                height * (1 - colliderMarginUp - colliderMarginDown));

        double movementArea = windowWidth - width;
        double leftLimit = 0;
        if (x > 3 * movementArea / 4) {
            leftLimit = x - movementArea / 2;
        } else if (x > movementArea / 4) {
            leftLimit = x - movementArea / 4;
        }
        double rightLimit = leftLimit + movementArea / 2;

        collider.setX(leftLimit);

        animTime = speed * 2;
        leftMovement = new Timeline(new KeyFrame(Duration.seconds(animTime), new KeyValue(this.xProperty(), rightLimit),
                new KeyValue(collider.xProperty(), rightLimit)));
        rightMovement = new Timeline(new KeyFrame(Duration.seconds(animTime), new KeyValue(this.xProperty(), leftLimit),
                new KeyValue(collider.xProperty(), leftLimit)));
        leftMovement.setOnFinished(event -> rightMovement.playFromStart());
        rightMovement.setOnFinished(event -> leftMovement.playFromStart());
        leftMovement.playFrom(Duration.seconds((rightLimit % 5) * animTime / 5));
    }

    public void moveToLower(double x, double y) {
        TranslateTransition t = new TranslateTransition(Duration.seconds(3), this);
        t.setToY(y);
        t.setCycleCount(1);
        t.setInterpolator(Interpolator.LINEAR);
        t.play();
    }

    public void moveToUpper(double x, double y) {
        TranslateTransition t = new TranslateTransition(Duration.seconds(3), this);
        t.setToY(-y);
        t.setCycleCount(1);
        t.setInterpolator(Interpolator.LINEAR);
        t.play();
    }
}
