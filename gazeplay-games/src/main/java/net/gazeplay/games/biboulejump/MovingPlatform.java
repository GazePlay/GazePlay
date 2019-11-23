package net.gazeplay.games.biboulejump;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.AnimationSpeedRatioSource;

@Slf4j
public class MovingPlatform extends Platform {

    public MovingPlatform(
        double x, double y,
        double width, double height,
        String soundFileLocation,
        double windowWidth,
        double colliderMarginUp, double colliderMarginRight, double colliderMarginDown, double colliderMarginLeft,
        int bounceFactor,
        AnimationSpeedRatioSource animationSpeedRatioSource
    ) {
        super(x, y,
            width, height,
            soundFileLocation,
            bounceFactor,
            colliderMarginUp, colliderMarginRight, colliderMarginDown, colliderMarginLeft);

        double mouvementArea = windowWidth - width;
        double leftLimit = 0;
        if (x > 3 * mouvementArea / 4) {
            leftLimit = x - mouvementArea / 2;
        } else if (x > mouvementArea / 4) {
            leftLimit = x - mouvementArea / 4;
        }
        double rightLimit = leftLimit + mouvementArea / 2;

        collider.setX(leftLimit);
        setX(leftLimit);

        Timeline leftMovement = new Timeline(new KeyFrame(Duration.seconds(2),
            new KeyValue(this.xProperty(), rightLimit), new KeyValue(collider.xProperty(), rightLimit)));
        Timeline rightMovement = new Timeline(new KeyFrame(Duration.seconds(2),
            new KeyValue(this.xProperty(), leftLimit), new KeyValue(collider.xProperty(), leftLimit)));

        SequentialTransition animation = new SequentialTransition();
        animation.getChildren().addAll(leftMovement, rightMovement);
        animation.setCycleCount(-1);
        animation.rateProperty().bind(animationSpeedRatioSource.getSpeedRatioProperty());
        animation.playFrom(Duration.seconds((rightLimit % 5d) * 2d / 5d));
    }

}
