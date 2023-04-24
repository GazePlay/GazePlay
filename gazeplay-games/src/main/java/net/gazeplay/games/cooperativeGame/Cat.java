package net.gazeplay.games.cooperativeGame;

import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;


public class Cat extends Parent {


    protected final Rectangle hitbox;
    protected final IGameContext gameContext;
    protected final CooperativeGame gameInstance;
    protected double speed;
    protected boolean isACat;
    protected Rectangle target;
    private KeyCode lastDirection = null;
    private AnimationTimer animationTimerCat;
    private double dirX = 0;
    private double dirY = 0;
    private final EventHandler<Event> enterEvent;




    public Cat(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
               final CooperativeGame gameInstance, double speed, boolean isACat){
        this.hitbox = new Rectangle(positionX, positionY, width, height);
        this.hitbox.setFill(Color.BLACK);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.speed = speed;
        this.isACat = isACat;
        this.enterEvent = null;

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

    public Cat(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
               final CooperativeGame gameInstance, double speed, boolean isACat, Rectangle target){
        this.hitbox = new Rectangle(positionX, positionY, width, height);
        this.hitbox.setFill(Color.YELLOW);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.speed = speed;
        this.isACat = isACat;
        this.target = target;

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

            gameContext.getPrimaryScene().setOnMouseMoved(mouseEvent ->{
                gameInstance.mouse.setX(mouseEvent.getX());
                gameInstance.mouse.setY(mouseEvent.getY());
            });


        }

        this.enterEvent = buildEvent();
        gameContext.getGazeDeviceManager().addEventFilter(this);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

    }

    private EventHandler<Event> buildEvent() {

        return e -> {
            if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                System.out.println("gaze entered");
            }
        };
    }


}
