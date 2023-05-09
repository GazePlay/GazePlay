package net.gazeplay.games.surviveAgainstRobots;


import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;


public class Bullet extends Rectangle {
    private final double speed;
    private SurviveAgainstRobots gameInstance;
    private IGameContext gameContext;
    private boolean isDestroyed;
    private double x,y,width,height;
    public Bullet(double x, double y, double width, double height, double speed, SurviveAgainstRobots gameInstance, IGameContext gameContext) {
        super(x, y, width, height);
        setFill(Color.RED);
        this.gameInstance = gameInstance;
        this.speed = speed;
        this.gameContext = gameContext;
        this.isDestroyed = false;
    }

    public void startMoving(KeyCode direction) {

        Timeline animation = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            if (!isDestroyed){
                if (direction == KeyCode.UP) {
                    setY(getY() - speed);
                } else if (direction == KeyCode.DOWN) {
                    setY(getY() + speed);
                } else if (direction == KeyCode.LEFT) {
                    setX(getX() - speed);
                } else if (direction == KeyCode.RIGHT) {
                    setX(getX() + speed);
                }
                if (gameInstance.willCollideWithAnObstacle(direction.toString().toLowerCase(), speed, this)) {
                    isDestroyed = true;
                }
            }
        }));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();

        if (isDestroyed){
            animation.stop();
        }
    }
}
