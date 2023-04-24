package net.gazeplay.games.cooperativeGame;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
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
    private AnimationTimer animationTimerCat;
    private EventHandler<Event> enterEvent = null;

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
                    case UP, DOWN -> dirY = 0;
                    case LEFT, RIGHT -> dirX = 0;
                    default -> {
                    }
                }
            });

            // Set the last direction of movement to null if the cat is not currently moving
            if (dirX == 0 && dirY == 0) {
                lastDirection = null;
            }

            // Create a new animation timer to handle movement
            animationTimerCat = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (!gameInstance.endOfLevel){
                        // Move the cat if it is currently moving and will not collide with an obstacle
                        if (dirX != 0 || dirY != 0) {
                            if (!gameInstance.willCollideWithAnObstacle(lastDirection.toString().toLowerCase(), speed, hitbox)) {
                                hitbox.setX(hitbox.getX() + dirX);
                                hitbox.setY(hitbox.getY() + dirY);
                            }
                        }
                    }else{
                        animationTimerCat.stop();
                    }
                }
            };

            // Start the animation timer
            animationTimerCat.start();
        }
    }

    public CatMovement(double positionX, double positionY, double width, double height, IGameContext gameContext, Stats stats, CooperativeGame gameInstance, double speed, boolean isACat, Rectangle target) {
        super(positionX, positionY, width, height, gameContext, stats, gameInstance, speed, isACat, target);

        if (!isACat){

            AnimationTimer animationTimerDog = new AnimationTimer() {
                double speedSave = speed;
                @Override
                public void handle(long now) {
                    if (!gameInstance.endOfLevel){
                        if (hitbox.getX() < target.getX()) {
                            if (!gameInstance.willCollideWithAnObstacle("right", speedSave, hitbox)) {
                                hitbox.setX(hitbox.getX() + speedSave);
                            }
                        } else if (hitbox.getX() > target.getX()) {
                            if (!gameInstance.willCollideWithAnObstacle("left", speedSave, hitbox)) {
                                hitbox.setX(hitbox.getX() - speedSave);
                            }
                        }

                        if (hitbox.getY() < target.getY()) {
                            if (!gameInstance.willCollideWithAnObstacle("down", speedSave, hitbox)) {
                                hitbox.setY(hitbox.getY() + speedSave);
                            }
                        } else if (hitbox.getY() > target.getY()) {
                            if (!gameInstance.willCollideWithAnObstacle("up", speedSave, hitbox)) {
                                hitbox.setY(hitbox.getY() - speedSave);
                            }
                        }

                        if(gameInstance.isCollidingWithASpecificObstacle(gameInstance.mouse,hitbox)) {
                            speedSave = 0;
                        }else{
                            speedSave = speed;
                        }
                    }else{
                        this.stop();
                    }

                }
            };
            animationTimerDog.start();

            enterEvent = buildEvent();

            gameContext.getPrimaryScene().setOnMouseMoved(mouseEvent ->{
                gameInstance.mouse = new Rectangle(mouseEvent.getX(),mouseEvent.getY(), hitbox.getWidth()/4,hitbox.getHeight()/4);
            });

            gameContext.getGazeDeviceManager().addEventFilter(this);

        }

    }

        private EventHandler<Event> buildEvent() {

            return e -> {

                if (e.getEventType() == GazeEvent.GAZE_ENTERED){
                    System.out.println("gaze entered");
                }

                if (e.getEventType() == GazeEvent.GAZE_EXITED){
                    System.out.println("gaze exited");
                }


            };
        }

}
