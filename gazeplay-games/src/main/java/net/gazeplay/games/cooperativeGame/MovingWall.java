package net.gazeplay.games.cooperativeGame;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

/**
 * Represents a moving wall object that can move either vertically or horizontally.
 */
public class MovingWall extends Rectangle {

    /** The direction of the moving wall. */
    private boolean direction;

    /** The cooperative game instance. */
    private final SprintFinish gameInstance;

    /** The timeline for vertical movement. */
    private Timeline verticalTimeline;

    /** The timeline for horizontal movement. */
    private Timeline horizontalTimeline;

    /** Indicates whether the wall should reset the object it collide with. */
    protected boolean resetPos;

    /** The game context in which the wall is present. */
    private final IGameContext gameContext;

    /** The event handler for gaze and mouse events. */
    private final EventHandler<Event> enterEvent;

    /** The speed of the moving wall. */
    private final float speed;

    /** Indicates whether the wall can move or not. */
    protected boolean canMove;


    MovingWall(final double x, final double y, final double width, final double height, SprintFinish gameInstance, IGameContext gameContext, boolean resetPos, float speed) {
        super(x, y, width, height);
        this.direction = true;
        this.gameInstance = gameInstance;
        this.resetPos = resetPos;
        this.gameContext = gameContext;
        this.verticalTimeline = new Timeline();
        this.horizontalTimeline = new Timeline();
        this.speed = speed;
        this.canMove = true;
        this.enterEvent = buildEvent();
        gameContext.getGazeDeviceManager().addEventFilter(this);
        this.addEventFilter(GazeEvent.ANY, enterEvent);
        this.addEventFilter(MouseEvent.ANY, enterEvent);
    }


    /**
     * This method animates the vertical movement of a MovingWall object.
     * @param fromY The initial Y-coordinate of the object.
     * @param toY The final Y-coordinate of the object.
     */
    public void animationVertical(double fromY, double toY){
        boolean frombottom;

        // check if the movement is from bottom to top or vice versa
        if (fromY > toY) {
            double temp = fromY;
            fromY = toY;
            toY = temp;
            frombottom = true;
        } else {
            frombottom = false;
        }

        double finalToY = toY;
        double finalFromY = fromY;

        // create a new timeline for the vertical animation
        verticalTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            if (canMove) { // check if the object can move
                this.setFill(Color.RED);
                if (this.direction) { // check if the object is moving up or down
                    gameInstance.willCollideWithAnObstacle("down", this.speed, this); // check if the object will collide with an obstacle below it
                    gameInstance.willCollideWithAnObstacle("up", this.speed, this); // check if the object will collide with an obstacle above it

                    if (frombottom) {
                        if (this.getY() + this.getHeight() >= finalToY + getHeight()) { // check if the object has reached its destination
                            this.direction = false; // change the direction of the movement
                        }
                    } else {
                        if (this.getY() + this.getHeight() >= finalToY) { // check if the object has reached its destination
                            this.direction = false; // change the direction of the movement
                        }
                    }

                    this.setY(this.getY() + this.speed); // move the object vertically
                } else {
                    gameInstance.willCollideWithAnObstacle("up", this.speed, this); // check if the object will collide with an obstacle above it
                    gameInstance.willCollideWithAnObstacle("down", this.speed, this); // check if the object will collide with an obstacle below it

                    if (this.getY() <= finalFromY) { // check if the object has reached its destination
                        this.direction = true; // change the direction of the movement
                    }

                    this.setY(this.getY() - this.speed); // move the object vertically
                }
            }else{
                this.setFill(Color.BLUE);
            }
        }));

        verticalTimeline.setCycleCount(Animation.INDEFINITE); // set the animation to run indefinitely
        verticalTimeline.play(); // start the animation
    }

    /**
     * Animates the movement of the wall horizontally from the given start position to the given end position.
     *
     * @param fromX the starting x-coordinate of the wall
     * @param toX the ending x-coordinate of the wall
     */
    public void animationHorizontal(double fromX, double toX){

        // Determine the direction of the movement
        boolean fromRight;
        if (fromX > toX) {
            double temp = fromX;
            fromX = toX;
            toX = temp;
            fromRight = true;
        }else{
            fromRight = false;
        }

        // Set the final x-coordinates of the wall
        double finalToX = toX;
        double finalFromX = fromX;

        // Create a timeline to animate the wall's movement
        horizontalTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {

            // Check if the wall can move
            if (canMove){
                this.setFill(Color.RED);
                // Check for collisions with obstacles in the game
                if (this.direction){
                    gameInstance.willCollideWithAnObstacle("right",this.speed,this);
                    gameInstance.willCollideWithAnObstacle("left",this.speed,this);

                    // Check if the wall has reached its destination and change its direction if necessary
                    if (fromRight){
                        if (this.getX() + this.getWidth() >= finalToX+getWidth()){
                            this.direction = false;
                        }
                    }else{
                        if (this.getX() + this.getWidth() >= finalToX){
                            this.direction = false;
                        }
                    }

                    // Move the wall to the right
                    this.setX(this.getX()+this.speed);

                }else{
                    gameInstance.willCollideWithAnObstacle("right",this.speed,this);
                    gameInstance.willCollideWithAnObstacle("left",this.speed,this);

                    // Check if the wall has reached its destination and change its direction if necessary
                    if (this.getX() <= finalFromX){
                        this.direction = true;
                    }

                    // Move the wall to the left
                    this.setX(this.getX()-this.speed);
                }
            }else{

                this.setFill(Color.BLUE);
            }
        }));
        // Set the cycle count of the animation to indefinite and play it
        horizontalTimeline.setCycleCount(Animation.INDEFINITE);
        horizontalTimeline.play();
    }

    private EventHandler<Event> buildEvent() {
        return e -> {
            if (gameInstance.gameTimerEnded){
                if (gameInstance.endOfLevel){
                    verticalTimeline.stop();
                    horizontalTimeline.stop();
                    this.canMove = false;
                }else{
                    if (!gameInstance.catNotKeyboard){
                        if (e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                            this.canMove = false;
                        }
                        if (e.getEventType() == GazeEvent.GAZE_EXITED || e.getEventType() == MouseEvent.MOUSE_EXITED){
                            this.canMove = true;
                        }
                    }
                }
            }
        };
    }




}

