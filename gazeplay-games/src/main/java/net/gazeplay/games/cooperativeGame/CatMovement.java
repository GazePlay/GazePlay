package net.gazeplay.games.cooperativeGame;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import javafx.scene.input.KeyEvent;

/**
 * Class for managing the movement of the hitbox character in the cooperative game.
 * This class inherits from the Cat class.
 */
public class CatMovement extends Cat{


    private Timeline timeline;
    private KeyCode lastDirection = null;
    private AnimationTimer animationTimer;
    private double dirX = 0;
    private double dirY = 0;

    /**
     * Constructor for the CatMovement class, which controls the movement of the cat character.
     * @param positionX the starting X position of the cat
     * @param positionY the starting Y position of the cat
     * @param width the width of the cat hitbox
     * @param height the height of the cat hitbox
     * @param gameContext the game context containing information about the game environment
     * @param stats the game statistics tracker
     * @param gameInstance the game instance
     * @param speed the speed at which the cat moves
     * @param isACat boolean indicating whether this instance is a cat or not
     */
    public CatMovement(double positionX, double positionY, double width, double height, IGameContext gameContext, Stats stats, CooperativeGame gameInstance, double speed, boolean isACat) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance, speed, isACat );

        // Set up key event listeners to handle cat movement
        if (isACat){
            // Add a key pressed event filter to the primary game scene to handle movement
            gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, key-> {
                if (key.getCode() == KeyCode.UP || key.getCode() == KeyCode.DOWN || key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.RIGHT) {

                    // Set the direction of movement based on the key pressed
                    switch (key.getCode()) {
                        case UP -> {
                            dirY = -speed;
                            lastDirection = KeyCode.UP;
                        }
                        case DOWN -> {
                            dirY = speed;
                            lastDirection = KeyCode.DOWN;
                        }
                        case LEFT -> {
                            dirX = -speed;
                            lastDirection = KeyCode.LEFT;
                        }
                        case RIGHT -> {
                            dirX = speed;
                            lastDirection = KeyCode.RIGHT;
                        }
                        default -> {
                            break;
                        }
                    }
                }
            });

            // Add a key released event filter to the primary game scene to handle stopping movement
            gameContext.getPrimaryScene().setOnKeyReleased(event -> {
                switch (event.getCode()) {
                    case UP -> dirY = 0;
                    case DOWN -> dirY = 0;
                    case LEFT -> dirX = 0;
                    case RIGHT -> dirX = 0;
                    default -> {
                    }
                }
            });

            // Set the last direction of movement to null if the cat is not currently moving
            if (dirX == 0 && dirY == 0) {
                lastDirection = null;
            }

            // Create a new animation timer to handle movement
            animationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // Move the cat if it is currently moving and will not collide with an obstacle
                    if (dirX != 0 || dirY != 0) {
                        if (!gameInstance.willCollideWithAnObstacle(lastDirection.toString().toLowerCase(), speed, hitbox)) {
                            hitbox.setX(hitbox.getX() + dirX);
                            hitbox.setY(hitbox.getY() + dirY);
                        }
                    }
                }
            };

            // Start the animation timer
            animationTimer.start();
        }
    }

    public CatMovement(double positionX, double positionY, double width, double height, IGameContext gameContext, Stats stats, CooperativeGame gameInstance, double speed, boolean isACat, Rectangle target) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance, speed, isACat, target);

        if (!isACat){
            followCatWithAnimation();
        }
    }

    private void followCatWithAnimation() {

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (hitbox.getX() < target.getX()) {
                    if (!gameInstance.willCollideWithAnObstacle("right", speed, hitbox)) {
                        hitbox.setX(hitbox.getX() + speed);
                    }
                } else if (hitbox.getX() > target.getX()) {
                    if (!gameInstance.willCollideWithAnObstacle("left", speed, hitbox)) {
                        hitbox.setX(hitbox.getX() - speed);
                    }
                }

                if (hitbox.getY() < target.getY()) {
                    if (!gameInstance.willCollideWithAnObstacle("down", speed, hitbox)) {
                        hitbox.setY(hitbox.getY() + speed);
                    }
                } else if (hitbox.getY() > target.getY()) {
                    if (!gameInstance.willCollideWithAnObstacle("up", speed, hitbox)) {
                        hitbox.setY(hitbox.getY() - speed);
                    }
                }
            }
        };



        animationTimer.start();



    }
}
