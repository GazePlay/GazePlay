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
    private double directionX;
    private double directionY;
    private IGameContext gameContext;
    private boolean isDestroyed;
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


    public void startMovingMouse(Rectangle target){
        // Calculate the difference in position between the mouse and the ball
        double dx = target.getX() - (getX() + getWidth() / 2);
        double dy = target.getY() - (getY() + getHeight() / 2);

        // Calculate the angle between the mouse and the ball using the trigonometric function atan2
        double angle = Math.atan2(dy, dx);

        // Calculate the direction of the ball's movement from the angle and speed
        directionX = speed * Math.cos(angle);
        directionY = speed * Math.sin(angle);

        // Define the animation for the ball movement
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            // Check if the ball has not been destroyed
            if (!isDestroyed){
                // Move the ball according to the direction calculated earlier
                setX(getX() + directionX);
                setY(getY() + directionY);

                // Check for collision of the ball with an obstacle (wall)
                if (gameInstance.willCollideWithAnObstacle("up", speed, this) ||
                    gameInstance.willCollideWithAnObstacle("down", speed, this) ||
                    gameInstance.willCollideWithAnObstacle("left", speed, this) ||
                    gameInstance.willCollideWithAnObstacle("right", speed, this) ){
                    // Set the ball as destroyed if it hits an obstacle
                    isDestroyed = true;
                }
            }
        }));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();

        // Stop the animation if the ball is destroyed
        if (isDestroyed){
            animation.stop();
        }
    }
}
