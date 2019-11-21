package net.gazeplay.games.biboulejump;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MovingPlatform extends Platform {

    private final Timeline leftMovement;

    private final Timeline rightMovement;

    public MovingPlatform(
        double x, double y,
        double width, double height,
        String soundFileLocation,
        double windowWidth,
        double colliderMarginUp, double colliderMarginRight, double colliderMarginDown, double colliderMarginLeft,
        int bounceFactor,
        double speed
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

        double animationTime = speed * 2;
        leftMovement = new Timeline(new KeyFrame(Duration.seconds(animationTime),
            new KeyValue(this.xProperty(), rightLimit), new KeyValue(collider.xProperty(), rightLimit)));
        rightMovement = new Timeline(new KeyFrame(Duration.seconds(animationTime),
            new KeyValue(this.xProperty(), leftLimit), new KeyValue(collider.xProperty(), leftLimit)));
        leftMovement.setOnFinished(event -> rightMovement.playFromStart());
        rightMovement.setOnFinished(event -> leftMovement.playFromStart());

        leftMovement.playFrom(Duration.seconds((rightLimit % 5) * animationTime / 5));
    }

}
