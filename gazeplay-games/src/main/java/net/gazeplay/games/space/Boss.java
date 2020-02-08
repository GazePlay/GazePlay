package net.gazeplay.games.space;

import javafx.animation.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;

public class Boss extends Rectangle {
    @Getter
    private final String soundFileLocation;
    @Getter
    private final Rectangle collider;
    private final Timeline leftMovement;
    private final Timeline rightMovement;
    private final int healthPoint;

    public Boss(final double x, final double y, final double width, final double height, final String soundFileLocation, final double windowWidth,
                final double speed) {
        this(x, y, width, height, soundFileLocation, windowWidth, speed, 0, 0, 0, 0);
    }

    public Boss(final double x, final double y, final double width, final double height, final String soundFileLocation, final double windowWidth,
                final double speed, final double colliderMarginUp, final double colliderMarginRight, final double colliderMarginDown,
                final double colliderMarginLeft) {
        super(x, y, width, height);
        this.soundFileLocation = soundFileLocation;
        this.collider = new Rectangle(x + colliderMarginLeft * width, y + colliderMarginUp * height,
            width * (1 - colliderMarginLeft - colliderMarginRight),
            height * (1 - colliderMarginUp - colliderMarginDown));
        this.healthPoint = 125;

        final double movementArea = windowWidth - width;
        double leftLimit = 0;
        if (x > 3 * movementArea / 4) {
            leftLimit = x - movementArea / 2;
        } else if (x > movementArea / 4) {
            leftLimit = x - movementArea / 4;
        }
        final double rightLimit = leftLimit + movementArea / 2;

        collider.setX(leftLimit);

        final double animTime = speed * 2;
        leftMovement = new Timeline(new KeyFrame(Duration.seconds(animTime), new KeyValue(this.xProperty(), rightLimit),
            new KeyValue(collider.xProperty(), rightLimit)));
        rightMovement = new Timeline(new KeyFrame(Duration.seconds(animTime), new KeyValue(this.xProperty(), leftLimit),
            new KeyValue(collider.xProperty(), leftLimit)));
        leftMovement.setOnFinished(event -> rightMovement.playFromStart());
        rightMovement.setOnFinished(event -> leftMovement.playFromStart());
        leftMovement.playFrom(Duration.seconds((rightLimit % 5) * animTime / 5));
    }

    public void moveToLower(final double x, final double y) {
        final TranslateTransition t = new TranslateTransition(Duration.seconds(3), this);
        t.setToY(y);
        t.setCycleCount(1);
        t.setInterpolator(Interpolator.LINEAR);
        t.play();
    }

    public void moveToUpper(final double x, final double y) {
        final TranslateTransition t = new TranslateTransition(Duration.seconds(3), this);
        t.setToY(-y);
        t.setCycleCount(1);
        t.setInterpolator(Interpolator.LINEAR);
        t.play();
    }

    public void moveToLeft(final double x, final double y) {
        final TranslateTransition t = new TranslateTransition(Duration.seconds(3), this);
        t.setToX(-x);
        t.setCycleCount(1);
        t.setInterpolator(Interpolator.LINEAR);
        t.play();
    }

    public void moveToLRight(final double x, final double y) {
        final TranslateTransition t = new TranslateTransition(Duration.seconds(3), this);
        t.setToY(x);
        t.setCycleCount(1);
        t.setInterpolator(Interpolator.LINEAR);
        t.play();
    }

    public int getHealthPoint() {
        return this.healthPoint;
    }

}
