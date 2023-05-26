package net.gazeplay.games.surviveAgainstRobots;

import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.animation.AnimationTimer;
import net.gazeplay.IGameContext;
import javafx.event.Event;
import javafx.event.EventHandler;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.util.Random;
/**
 * Represents a player in the game.
 */
public class Player extends Parent {

    protected Rectangle hitbox;
    private final double speed;
    private final IGameContext gameContext;
    private final SurviveAgainstRobots gameInstance;
    private KeyCode horizontalDirection, verticalDirection;

    private double dirX,dirY;
    private double currentSpeedX;
    private double currentSpeedY;
    private final double acceleration;
    protected AnimationTimer playerAnimationMovement;
    protected double freqShoot;
    private final double bulletSpeed;
    private Rectangle target;
    private final EventHandler<Event> enterEvent;
    protected boolean isDead;

    private Random random;


    /**
     * Constructs a new player object with the given parameters.
     *
     * @param hitbox the hitbox of the player
     * @param speed the speed of the player
     * @param gameContext the context of the game
     * @param gameInstance the instance of the game
     * @param freqShoot2 the frequency of the player's shooting
     * @param target the target of the player
     */
    public Player(Rectangle hitbox, double speed, IGameContext gameContext, SurviveAgainstRobots gameInstance, double freqShoot2, Rectangle target) {
        this.hitbox = hitbox;
        this.speed = speed;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.acceleration = 0.5;
        this.freqShoot = freqShoot2;
        this.isDead = false;
        this.random = new Random();
        pickImagePlayer();

        if (target != null){
            this.bulletSpeed = (this.speed+5) *1.8;
            this.target = target;
            enterEvent = buildEvent();

            gameContext.getPrimaryScene().addEventFilter(GazeEvent.ANY, enterEvent);
            gameContext.getPrimaryScene().addEventFilter(MouseEvent.ANY, enterEvent);
        }else{
            this.bulletSpeed = this.speed*1.8;
            this.enterEvent = null;
            initKey();
        }
            playerAnimationMovement = new AnimationTimer() {
                int nbframe = 0;
                @Override
                public void handle(long now) {
                    if (!isDead){
                        if (!gameContext.getChildren().contains(hitbox)){
                            isDead = true;
                        }
                        if (target==null){
                            playerMove();
                        }else{
                            playerMoveMouse();
                        }

                        if (nbframe == 60*freqShoot){
                            nbframe = 0;
                        }else if (nbframe == 60){
                            nbframe = 0;
                        }
                        if (nbframe == 0){
                            if (target == null && !gameInstance.automaticShoot){
                                automaticShoot();
                            } else if (gameInstance.automaticShoot){
                                automaticTargetShoot();

                            } else{
                                automaticShootMouse();
                                hitbox.toFront();
                            }
                        }
                        nbframe++;
                    }else{
                        stop();
                    }
                }
            };
            playerAnimationMovement.start();
    }


    /**
     * Builds and returns an event handler for the player's target based on the current game context.
     * The target represent the mouse he will follow.
     * @return the event handler for the player's target
     */
    private EventHandler<Event> buildEvent(){
        return e -> {

            if (e.getEventType() == GazeEvent.GAZE_MOVED && this.target != null){
                this.target.setX(((GazeEvent)e).getX());
                this.target.setY(((GazeEvent)e).getY());
            }else if (e.getEventType() == MouseEvent.MOUSE_MOVED && this.target != null){
                this.target.setX(((MouseEvent)e).getX());
                this.target.setY(((MouseEvent)e).getY());
            }
        };
    }


    /**
     * Moves the player towards their target when controlled by the mouse.
     */
    private void playerMoveMouse(){
        double dx = target.getX() - hitbox.getX()-hitbox.getWidth()/2;
        double dy = target.getY() - hitbox.getY()- hitbox.getHeight()/2;

        double distance = Math.sqrt(dx * dx + dy * dy);

        if(distance > speed) {
            double vx = dx / distance * speed;
            double vy = dy / distance * speed;

            String directionX = vx > 0 ? "right" : "left";
            String directionY = vy > 0 ? "down" : "up";

            if(Math.abs(vx) > Math.abs(vy)) {


                if(!gameInstance.willCollideWithAnObstacle(directionX, speed, hitbox)) {
                    hitbox.setX(hitbox.getX() + vx);
                }
                if(!gameInstance.willCollideWithAnObstacle(directionY, speed, hitbox)) {
                    hitbox.setY(hitbox.getY() + vy);
                }
            } else {

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
     * This method is used to automatically shoot a bullet at the nearest robot target from the player.
     * If there is a nearest robot target, a bullet will be created and added to the game context.
     * The bullet will start moving towards the nearest robot target.
     */
    private void automaticTargetShoot(){

        Robot nearestRobot = gameInstance.getNearestRobotFromPlayer();
        if (nearestRobot != null){
            double x = this.hitbox.getX() + this.hitbox.getWidth() / 2;
            double y = this.hitbox.getY() + this.hitbox.getHeight() / 2;
            Bullet bullet = new Bullet(x, y, 10, 10, bulletSpeed,gameInstance, gameContext);
            bullet.setId("playerBullet");
            gameContext.getChildren().add(bullet);
            bullet.startMovingTowardTarget(nearestRobot);
        }
    }

    /**
     * This method is used to automatically shoot a bullet towards the mouse.
     * A bullet will be created and added to the game context.
     * The bullet will start moving towards the mouse.
     */
    private void automaticShootMouse(){
        double x = this.hitbox.getX() + this.hitbox.getWidth() / 2;
        double y = this.hitbox.getY() + this.hitbox.getHeight() / 2;
        Bullet bullet = new Bullet(x, y, 10, 10, bulletSpeed,gameInstance, gameContext);
        bullet.setId("playerBullet");

        gameContext.getChildren().add(bullet);
        bullet.startMovingTowardTarget(target);


    }


    /**
     * This method is used to automatically shoot a bullet in a direction based on the player's movement direction.
     * A bullet will be created and added to the game context.
     * The bullet will start moving in the direction of the player's movement.
     */
    private void automaticShoot(){

        if (horizontalDirection != null) {

            Bullet bullet;
            if(horizontalDirection == KeyCode.LEFT){
                bullet = new Bullet(hitbox.getX(), hitbox.getY() + hitbox.getHeight() / 2,10,10, bulletSpeed, gameInstance, gameContext);
            }else{
                bullet = new Bullet(hitbox.getX() + hitbox.getWidth(), hitbox.getY() + hitbox.getHeight() / 2, 10,10,bulletSpeed, gameInstance, gameContext);
            }
            bullet.setId("playerBullet");
            gameContext.getChildren().add(bullet);
            bullet.startMoving(horizontalDirection);
        }else if (verticalDirection != null) {
            Bullet bullet;
            if (verticalDirection == KeyCode.UP){
                bullet = new Bullet(hitbox.getX() + hitbox.getWidth() / 2, hitbox.getY(),10,10, bulletSpeed, gameInstance, gameContext);
            }else{
                bullet = new Bullet(hitbox.getX() + hitbox.getWidth() / 2, hitbox.getY() + hitbox.getHeight(),10,10, bulletSpeed, gameInstance, gameContext);
            }
            bullet.setId("playerBullet");
            gameContext.getChildren().add(bullet);
            bullet.startMoving(verticalDirection);

        }
    }


    /**
     * Updates the player's movement based on the current horizontal and vertical directions,
     * and moves the player's hitbox accordingly if there are no obstacles in the way.
     * If both horizontal and vertical directions are set, the player's speed is normalized
     * to 70.7% of the maximum speed to ensure consistent movement in both directions.
     */
    private void playerMove(){
        // Check if there is any direction of movement
        if (dirX != 0 ||dirY != 0){
            // Initialize variables for checking if the player can move vertically or horizontally
            boolean canMoveVertically;
            boolean canMoveHorizontally;

            // Adjust current speed of player based on its vertical and horizontal direction
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

            // Adjust current speed of player if it moves in both vertical and horizontal directions
            if (horizontalDirection != null && verticalDirection != null){
                double factor = 1 / Math.sqrt(2);

                if (currentSpeedX > 0){
                    currentSpeedX = speed;
                }else{
                    currentSpeedX = -speed;
                }

                if (currentSpeedY > 0){
                    currentSpeedY = speed;
                }else{
                    currentSpeedY = -speed;
                }

                currentSpeedX *= factor;
                currentSpeedY *= factor;
            }

            // Check if the player can move vertically and horizontally without colliding with an obstacle
            canMoveVertically = verticalDirection != null && !gameInstance.willCollideWithAnObstacle(verticalDirection.toString().toLowerCase(), speed, hitbox);
            canMoveHorizontally = horizontalDirection != null && !gameInstance.willCollideWithAnObstacle(horizontalDirection.toString().toLowerCase(), speed, hitbox);

            // Move the player based on its speed and direction of movement
            if (canMoveHorizontally){
                hitbox.setX(hitbox.getX() + currentSpeedX);
            }
            if (canMoveVertically){
                hitbox.setY(hitbox.getY() + currentSpeedY);
            }
        }

    }

    /**
     * Initializes the keyboard event listeners for the player movement,
     * allowing the player to move left, right, up, and down using the arrow keys.
     * When a key is pressed, the corresponding direction and speed are set.
     * When a key is released, the corresponding direction and speed are reset to 0.
     */
    public void initKey(){
        gameContext.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case LEFT -> {

                    dirX = -speed;
                    horizontalDirection = KeyCode.LEFT;

                }
                case RIGHT -> {

                    dirX = speed;
                    horizontalDirection = KeyCode.RIGHT;

                }
                case UP -> {
                    dirY = -speed;
                    verticalDirection = KeyCode.UP;
                }
                case DOWN -> {
                    dirY = speed;
                    verticalDirection = KeyCode.DOWN;
                }
                default -> {

                }
            }
        });

        gameContext.getPrimaryScene().setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT -> {
                    if(horizontalDirection != KeyCode.RIGHT){
                        horizontalDirection = null;
                        dirX = 0;
                        currentSpeedX = 0;
                    }

                }
                case RIGHT -> {
                    if(horizontalDirection != KeyCode.LEFT){
                        horizontalDirection = null;
                        currentSpeedX = 0;
                        dirX = 0;
                    }
                }
                case UP -> {
                    if(verticalDirection != KeyCode.DOWN){
                        verticalDirection = null;
                        currentSpeedY = 0;
                        dirY = 0;
                    }
                }
                case DOWN -> {
                    if(verticalDirection != KeyCode.UP){
                        verticalDirection = null;
                        currentSpeedY = 0;
                        dirY = 0;
                    }
                }
                default -> {

                }
            }
        });
    }


    /**
     * this method pick a random image for the player
     */
    private void pickImagePlayer(){
        int randomIndex = random.nextInt(PlayerEnum.values().length);
        PlayerEnum bonus = PlayerEnum.values()[randomIndex];
        this.hitbox.setFill(bonus.getImage());
    }
}
