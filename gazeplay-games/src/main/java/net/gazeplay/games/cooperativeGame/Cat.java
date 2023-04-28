package net.gazeplay.games.cooperativeGame;

import javafx.animation.AnimationTimer;
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
    private KeyCode horizontalDirection;
    private KeyCode verticalDirection;
    private AnimationTimer animationTimerCat;
    private double dirX = 0;
    private double dirY = 0;
    private final EventHandler<Event> enterEvent;
    private boolean canMove;
    private double currentSpeedX;
    private double currentSpeedY;
    private double acceleration;
    protected double initPosX;
    protected double initPosY;




    public Cat(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
               final CooperativeGame gameInstance, double speed2, boolean isACat){
        this.hitbox = new Rectangle(positionX, positionY, width, height);
        this.hitbox.setFill(Color.BLACK);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.speed = speed2;
        this.isACat = isACat;
        this.enterEvent = null;
        this.acceleration = 0.6;
        this.currentSpeedX = 0;
        this.currentSpeedY = 0;

        // Set up key event listeners to handle cat movement
        if (isACat){


            // Add a key pressed event filter to the primary game scene to handle movement
            gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, key-> {
                if (key.getCode() == KeyCode.UP || key.getCode() == KeyCode.DOWN || key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.RIGHT) {



                    // Set the direction of movement based on the key pressed
                    switch (key.getCode()) {
                        case UP -> {

                            dirY = -speed;
                            verticalDirection = KeyCode.UP;

                        }
                        case DOWN -> {
                            dirY = speed;
                            verticalDirection = KeyCode.DOWN;

                        }
                        case LEFT -> {
                            dirX = -speed;
                            horizontalDirection = KeyCode.LEFT;

                        }
                        case RIGHT -> {
                            dirX = speed;
                            horizontalDirection = KeyCode.RIGHT;


                        }

                        default -> {

                        }
                    }
                }
            });

            // Add a key released event filter to the primary game scene to handle stopping movement
            gameContext.getPrimaryScene().setOnKeyReleased(event -> {
                switch (event.getCode()) {

                    case UP -> {
                        if (verticalDirection != KeyCode.DOWN){
                            dirY = 0;
                            verticalDirection = null;
                            currentSpeedY = 0;
                        }
                    }
                    case DOWN -> {
                        if (verticalDirection != KeyCode.UP){
                            dirY = 0;
                            verticalDirection = null;
                            currentSpeedY = 0;
                        }
                    }
                    case LEFT -> {
                        if (horizontalDirection != KeyCode.RIGHT){
                            dirX = 0;
                            currentSpeedX = 0;
                            horizontalDirection = null;
                        }
                    }
                    case RIGHT -> {
                        if (horizontalDirection != KeyCode.LEFT){
                            dirX = 0;
                            currentSpeedX = 0;
                            horizontalDirection = null;
                        }
                    }
                    default -> {
                    }
                }

            });

            // Set the last direction of movement to null if the cat is not currently moving
            if (dirX == 0 && dirY == 0) {
                verticalDirection = null;
                horizontalDirection = null;
                currentSpeedX = 0;
                currentSpeedY = 0;
            }

            // Create a new animation timer to handle movement
            animationTimerCat = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (!gameInstance.endOfLevel){
                        catMove();
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
               final CooperativeGame gameInstance, double speed2, boolean isACat, Rectangle target){
        this.hitbox = new Rectangle(positionX, positionY, width, height);
        this.hitbox.setFill(Color.YELLOW);

        this.initPosX = positionX;
        this.initPosY = positionY;

        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.speed = speed2;
        this.isACat = isACat;
        this.target = target;
        this.canMove = true;

        if (!isACat){

            AnimationTimer animationTimerDog = new AnimationTimer() {

                @Override
                public void handle(long now) {
                    if (!gameInstance.endOfLevel){
                        if (canMove){

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
                    }else{
                        this.stop();
                    }

                }
            };
            animationTimerDog.start();
        }

        this.enterEvent = buildEvent();
        gameContext.getGazeDeviceManager().addEventFilter(hitbox);
        this.hitbox.addEventFilter(GazeEvent.ANY, enterEvent);
        this.hitbox.addEventFilter(MouseEvent.ANY, enterEvent);

    }

    private EventHandler<Event> buildEvent() {
        return e -> {
            if (e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                this.hitbox.setFill(Color.BLUE);
                this.canMove = false;
            }
            if (e.getEventType() == GazeEvent.GAZE_EXITED || e.getEventType() == MouseEvent.MOUSE_EXITED){
                this.hitbox.setFill(Color.YELLOW);
                this.canMove = true;
            }
        };
    }

    private void catMove(){

        // Move the cat if it is currently moving and will not collide with an obstacle
        if (dirX != 0 || dirY != 0) {
            if (verticalDirection != null && verticalDirection == KeyCode.UP){
                if (currentSpeedY > 0){
                    currentSpeedY = -1;
                }
                if (currentSpeedY > dirY){
                    currentSpeedY -= acceleration;
                }
            }
            if (verticalDirection != null && verticalDirection == KeyCode.DOWN){
                if (currentSpeedY < 0){
                    currentSpeedY = 1;
                }
                if (currentSpeedY < dirY){
                    currentSpeedY += acceleration;
                }
            }
            if (horizontalDirection != null && horizontalDirection == KeyCode.LEFT){

                if (currentSpeedX > 0){
                    currentSpeedX = -1;
                }
                if (currentSpeedX > dirX){
                    currentSpeedX -= acceleration;
                }
            }
            if (horizontalDirection != null && horizontalDirection == KeyCode.RIGHT){

                if (currentSpeedX < 0){
                    currentSpeedX = 1;
                }
                if (currentSpeedX < dirX){
                    currentSpeedX += acceleration;
                }
            }

            if (verticalDirection != null && horizontalDirection == null && !gameInstance.willCollideWithAnObstacle(verticalDirection.toString().toLowerCase(), speed, hitbox)){

                hitbox.setY(hitbox.getY() + currentSpeedY);

            }else if (horizontalDirection != null && verticalDirection == null && !gameInstance.willCollideWithAnObstacle(horizontalDirection.toString().toLowerCase(), speed, hitbox)){

                hitbox.setX(hitbox.getX() + currentSpeedX);

            } else if (verticalDirection != null && horizontalDirection != null){



                if (!gameInstance.willCollideWithAnObstacle(verticalDirection.toString().toLowerCase(), speed, hitbox)){

                        hitbox.setY(hitbox.getY() + currentSpeedY);
                }
                if (!gameInstance.willCollideWithAnObstacle(horizontalDirection.toString().toLowerCase(), speed, hitbox)){

                    hitbox.setX(hitbox.getX() + currentSpeedX);
                }

            }

        }

    }

    public void initPos(){
        this.initPosX = this.hitbox.getX();
        this.initPosY = this.hitbox.getY();
    }

}
