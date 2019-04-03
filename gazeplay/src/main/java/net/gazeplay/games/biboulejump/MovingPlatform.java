package net.gazeplay.games.biboulejump;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class MovingPlatform extends Platform {

    private static double animationTime = 2;
    private Timeline leftMovement;
    private Timeline rightMovement;

    public MovingPlatform(double x, double y, double width, double height, String soundFileLocation, int bounceFactor, double windowWidth) {
        this(x, y, width, height, soundFileLocation, bounceFactor, windowWidth, 0, 0, 0, 0);
    }

    public MovingPlatform(double x, double y, double width, double height, String soundFileLocation, int bounceFactor, double windowWidth, double colliderMarginUp, double colliderMarginRight, double colliderMarginDown, double colliderMarginLeft) {
        super(x, y, width, height, soundFileLocation, bounceFactor, colliderMarginUp, colliderMarginRight, colliderMarginDown, colliderMarginLeft);

        double mouvementArea = windowWidth - width;
        double leftLimit = 0;
        if(x > 3*mouvementArea/4){
            leftLimit = x - mouvementArea/2;
        }else if(x > mouvementArea/4){
            leftLimit = x - mouvementArea/4;
        }
        double rightLimit = leftLimit + mouvementArea/2;

        collider.setX(leftLimit);
        setX(leftLimit);

        leftMovement = new Timeline(new KeyFrame(Duration.seconds(animationTime), new KeyValue(this.xProperty(), rightLimit), new KeyValue(collider.xProperty(), rightLimit)));
        rightMovement = new Timeline(new KeyFrame(Duration.seconds(animationTime), new KeyValue(this.xProperty(), leftLimit), new KeyValue(collider.xProperty(), leftLimit)));
        leftMovement.setOnFinished(event -> rightMovement.playFromStart());
        rightMovement.setOnFinished(event -> leftMovement.playFromStart());

        leftMovement.playFrom(Duration.seconds(animationTime * (x - leftLimit)/mouvementArea/2));
    }
}
