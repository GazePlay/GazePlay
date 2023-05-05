/**

 The Cat class represents a cat in the cooperative game.
 It extends the JavaFX Parent class and contains methods for moving the cat based on user input and
 for moving the cat automatically towards a target if it is not a player-controlled cat.
 */
package net.gazeplay.games.cooperativeGame;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;


public class Cat extends Parent {


    /**
     * The hitbox for the cat object
     */
    protected final Rectangle hitbox;

    /**
     * The game context
     */
    protected final IGameContext gameContext;

    /**
     * The game instance
     */
    protected final SprintFinish gameInstance;

    /**
     * The speed of the cat
     */
    protected double speed;

    /**
     * A boolean indicating whether the cat is a player-controlled cat
     */
    protected boolean isACat;

    /**
     * The target rectangle for the cat to move towards if it is not a player-controlled cat
     */
    protected Rectangle target;

    /**
     * The horizontal direction of the cat's movement
     */
    private KeyCode horizontalDirection;

    /**
     * The vertical direction of the cat's movement
     */
    private KeyCode verticalDirection;

    /**
     * The animation timer for the cat's movement
     */
    private AnimationTimer animationTimerCat;

    /**
     * The current X direction of movement
     */
    private double dirX = 0;

    /**
     * The current Y direction of movement
     */
    private double dirY = 0;

    /**
     * The enter event handler for the cat
     */
    private final EventHandler<Event> enterEvent;

    /**
     * A boolean indicating whether the cat can currently move
     */
    protected boolean canMove;

    /**
     * The current X speed of movement
     */
    private double currentSpeedX;

    /**
     * The current Y speed of movement
     */
    private double currentSpeedY;

    /**
     * The acceleration of the cat's movement
     */
    protected double acceleration;

    /**
     * The initial X position of the cat
     */
    protected double initPosX;

    /**
     * The initial Y position of the cat
     */
    protected double initPosY;



    /**
     * Constructs a new Cat or a Dog object with the specified position, dimensions, game context, stats, game instance, speed, player-controlled flag and the target.
     * @param positionX The X position of the cat
     * @param positionY The Y position of the cat
     * @param width The width of the cat
     * @param height The height of the cat
     * @param gameContext The game context
     * @param stats The game stats
     * @param gameInstance The game instance
     * @param speed2 The speed of the cat
     * @param isACat A boolean indicating whether the object is a cat or a dog
     * @param target A target that the dog will follow
     */
    public Cat(final double positionX, final double positionY, final double width, final double height, final IGameContext gameContext, final Stats stats,
               final SprintFinish gameInstance, double speed2, boolean isACat, Rectangle target){
        this.hitbox = new Rectangle(positionX, positionY, width, height);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.speed = speed2;
        this.isACat = isACat;
        this.acceleration = 0.6;
        this.currentSpeedX = 0;
        this.currentSpeedY = 0;

        // Set up key event listeners to handle cat movement

        // If the cat is playable with keyboard
        if (isACat && target == null){
            this.enterEvent = null;
            this.target = null;

            this.hitbox.setFill(new ImagePattern(new Image("data/cooperativeGame/chat.png")));
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

                    if (gameInstance.gameTimerEnded){
                        if (!gameInstance.endOfLevel){
                            catMove();
                        }else{
                            animationTimerCat.stop();
                        }
                    }
                }
            };


            // Start the animation timer
            animationTimerCat.start();


        } else if (isACat && target != null){ // If the cat is not playable with a keyboard
            this.hitbox.setFill(new ImagePattern(new Image("data/cooperativeGame/chat.png")));
            this.target = target;

            // Start moving after the game timer ended.
            AnimationTimer animationTimerCat = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (gameInstance.gameTimerEnded){
                        if (!gameInstance.endOfLevel){
                            dogMove();
                        }else{
                            this.stop();
                        }
                    }
                }
            };
            animationTimerCat.start();

            this.enterEvent = buildEvent();
            gameContext.getPrimaryScene().addEventFilter(GazeEvent.ANY, enterEvent);
            gameContext.getPrimaryScene().addEventFilter(MouseEvent.ANY, enterEvent);
        } else{ //If it's a dog

            this.hitbox.setFill(new ImagePattern(new Image("data/cooperativeGame/chien.png")));
            this.target = target;
            this.canMove = true;
            initPos();

            // Start moving after the game timer ended.
            AnimationTimer animationTimerDog = new AnimationTimer() {

                @Override
                public void handle(long now) {
                    if (gameInstance.gameTimerEnded){
                        if (!gameInstance.endOfLevel){
                            if (canMove){
                                dogMove();
                            }
                        }else{
                            this.stop();
                        }
                    }

                }
            };
            animationTimerDog.start();

            this.enterEvent = buildEvent();
            gameContext.getGazeDeviceManager().addEventFilter(hitbox);
            this.hitbox.addEventFilter(GazeEvent.ANY, enterEvent);
            this.hitbox.addEventFilter(MouseEvent.ANY, enterEvent);
        }


    }

    private EventHandler<Event> buildEvent() {
        return e -> {
            if (!gameInstance.catNotKeyboard){
                //dogs has to be watched to be stopped
                if (e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    this.canMove = false;
                }
                if (e.getEventType() == GazeEvent.GAZE_EXITED || e.getEventType() == MouseEvent.MOUSE_EXITED){
                    this.canMove = true;
                }
            }else{
                //The cursor has to be tracked so the cat can follow it
                if (e.getEventType() == GazeEvent.GAZE_MOVED && this.isACat && this.target != null){
                    this.target.setX(((GazeEvent)e).getX());
                    this.target.setY(((GazeEvent)e).getY());
                }else if (e.getEventType() == MouseEvent.MOUSE_MOVED && this.isACat && this.target != null){
                    this.target.setX(((MouseEvent)e).getX());
                    this.target.setY(((MouseEvent)e).getY());

                }
            }
        };
    }


    /**
     * Moves the dog towards its target.
     * If the distance to the target is greater than the dog's speed,
     * the dog moves a distance equal to its speed towards the target.
     * If the distance is less than or equal to the dog's speed, the dog moves to the target.
     * The dog's movement is checked against any obstacles to prevent collisions.
     */
    public void dogMove(){
        // Get the difference in x and y positions between the dog and its target
        double dx = target.getX() - hitbox.getX();
        double dy = target.getY() - hitbox.getY();

        // Calculate the distance between the dog and its target using Pythagorean theorem
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Move the dog towards its target if it's not already at the target
        if(distance > speed) {
            // Calculate the velocity vector (vx, vy) needed to move towards the target
            double vx = dx / distance * speed;
            double vy = dy / distance * speed;

            // Determine the direction of movement in the x and y axes
            String directionX = vx > 0 ? "right" : "left";
            String directionY = vy > 0 ? "down" : "up";

            // Move the dog in the direction with the largest velocity component first
            if(Math.abs(vx) > Math.abs(vy)) {

                // Move the dog in the x direction first
                // Check if the dog will collide with an obstacle before moving it
                if(!gameInstance.willCollideWithAnObstacle(directionX, speed, hitbox)) {
                    hitbox.setX(hitbox.getX() + vx);
                }
                if(!gameInstance.willCollideWithAnObstacle(directionY, speed, hitbox)) {
                    hitbox.setY(hitbox.getY() + vy);
                }
            } else {
                // Move the dog in the y direction first
                // Check if the dog will collide with an obstacle before moving it
                if(!gameInstance.willCollideWithAnObstacle(directionY, speed, hitbox)) {
                    hitbox.setY(hitbox.getY() + vy);
                }
                if(!gameInstance.willCollideWithAnObstacle(directionX, speed, hitbox)) {
                    hitbox.setX(hitbox.getX() + vx);
                }
            }
        }
    }



    /**
     * Moves the cat in the current direction if it will not collide with an obstacle.
     * The movement of the cat is affected by the acceleration and speed values.
     */
    private void catMove(){

        // Move the cat if it is currently moving and will not collide with an obstacle
        if (dirX != 0 || dirY != 0) {
            boolean canMoveVertically,canMoveHorizontally;

            // Calculate current speed for vertical and horizontal directions
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

            // Check if the cat can move vertically and horizontally without colliding with an obstacle
            canMoveVertically = verticalDirection != null && !gameInstance.willCollideWithAnObstacle(verticalDirection.toString().toLowerCase(), speed, hitbox);
            canMoveHorizontally = horizontalDirection != null && !gameInstance.willCollideWithAnObstacle(horizontalDirection.toString().toLowerCase(), speed, hitbox);

            // Move the cat in the specified direction
            if (canMoveVertically) {
                hitbox.setY(hitbox.getY() + currentSpeedY);
            }
            if (canMoveHorizontally) {
                hitbox.setX(hitbox.getX() + currentSpeedX);
            }

        }

    }


    public void initPos(){
        this.initPosX = this.hitbox.getX();
        this.initPosY = this.hitbox.getY();
    }

}
