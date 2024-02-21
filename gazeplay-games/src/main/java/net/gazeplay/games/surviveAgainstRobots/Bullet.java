package net.gazeplay.games.surviveAgainstRobots;


import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;


/**
 * This class represents a bullet object in the game. A bullet is a rectangular shape that moves in a certain direction with a given speed.
 * It is created with a specified position, size, speed, and associated game instance and game context.
 */
public class Bullet extends Rectangle {
    private final double speed;
    private final SurviveAgainstRobots gameInstance;
    private double directionX;
    private double directionY;
    private final IGameContext gameContext;
    private boolean isDestroyed;

    /**
     Creates a new Bullet object with the specified position, size, speed, game instance and game context.
     @param x the x position of the bullet.
     @param y the y position of the bullet.
     @param width the width of the bullet.
     @param height the height of the bullet.
     @param speed the speed at which the bullet will move.
     @param gameInstance the instance of the game that the bullet belongs to.
     @param gameContext the context of the game that the bullet belongs to.
     */
    public Bullet(double x, double y, double width, double height, double speed, SurviveAgainstRobots gameInstance, IGameContext gameContext) {
        super(x, y, width, height);
        setFill(Color.RED);
        this.gameInstance = gameInstance;
        this.speed = speed;
        this.gameContext = gameContext;
        this.isDestroyed = false;

    }

    /**
     * Starts the movement of the bullet in the specified direction
     * @param direction the direction in which the bullet should move
     */
    public void startMoving(KeyCode direction) {


        Timeline animation = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            if (!gameContext.getChildren().contains(this)){
                isDestroyed = true;
            }
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



    /**
     * Starts the movement of the bullet towards the target.
     * The direction and speed of the bullet are calculated based on the difference between the target
     * rectangle and the current position of the bullet. The bullet is then moved towards the target
     * by updating its x and y coordinates according to the direction calculated earlier.
     * If the bullet collides with an obstacle, it is destroyed and the animation stops.
     * @param target the rectangle representing the target towards which the bullet will be directed
     */
    public void startMovingTowardTarget(Rectangle target){
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
            if (!gameContext.getChildren().contains(this)){
                isDestroyed = true;
            }
            // Check if the ball has not been destroyed
            if (!isDestroyed){
                // Move the ball according to the direction calculated earlier
                setX(getX() + directionX);
                setY(getY() + directionY);

                // Check for collision of the ball with an obstacle (wall)
                if (gameInstance.willCollideWithAnObstacle("up", speed, this) ||
                    gameInstance.willCollideWithAnObstacle("down", speed, this)){
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
