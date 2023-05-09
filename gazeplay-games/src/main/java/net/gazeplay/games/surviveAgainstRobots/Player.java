package net.gazeplay.games.surviveAgainstRobots;

import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.animation.AnimationTimer;
import net.gazeplay.IGameContext;

public class Player extends Parent {

    protected Rectangle hitbox;
    private double speed;
    private final IGameContext gameContext;
    private final SurviveAgainstRobots gameInstance;
    private KeyCode horizontalDirection, verticalDirection;

    private double dirX,dirY;

    /**
     * The current X speed of movement
     */
    private double currentSpeedX;

    /**
     * The current Y speed of movement
     */
    private double currentSpeedY;
    private double acceleration;


    private AnimationTimer playerAnimationMovement;
    private double freqShoot;
    private double bulletSpeed;

    public Player(Rectangle hitbox, double speed, IGameContext gameContext, SurviveAgainstRobots gameInstance, double freqShoot) {
        this.hitbox = hitbox;
        this.speed = speed;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.acceleration = 0.5;
        this.freqShoot = freqShoot;
        this.bulletSpeed = this.speed *1.2;

        initKey();

        playerAnimationMovement = new AnimationTimer() {
            int nbframe = 0;
            @Override
            public void handle(long now) {
                playerMove();
                nbframe++;
                if (nbframe == 60*freqShoot){
                    nbframe = 0;
                }
                if (nbframe == 0){
                    automaticShoot();
                }
            }
        };
        playerAnimationMovement.start();
    }


    public void automaticShoot(){
        if (verticalDirection != null) {
            Bullet bullet;
            if (verticalDirection == KeyCode.UP){
                bullet = new Bullet(hitbox.getX() + hitbox.getWidth() / 2, hitbox.getY(),10,10, bulletSpeed, gameInstance, gameContext);
            }else{
                bullet = new Bullet(hitbox.getX() + hitbox.getWidth() / 2, hitbox.getY() + hitbox.getHeight(),10,10, bulletSpeed, gameInstance, gameContext);
            }
            gameContext.getChildren().add(bullet);
            bullet.startMoving(verticalDirection);

        } else if (horizontalDirection != null) {

            Bullet bullet;
            if(horizontalDirection == KeyCode.LEFT){
                bullet = new Bullet(hitbox.getX(), hitbox.getY() + hitbox.getHeight() / 2,10,10, bulletSpeed, gameInstance, gameContext);
            }else{
                bullet = new Bullet(hitbox.getX() + hitbox.getWidth(), hitbox.getY() + hitbox.getHeight() / 2, 10,10,bulletSpeed, gameInstance, gameContext);
            }
            gameContext.getChildren().add(bullet);
            bullet.startMoving(horizontalDirection);
        }
    }

    public void playerMove(){

        if (dirX != 0 ||dirY != 0){
            boolean canMoveVertically;
            boolean canMoveHorizontally;

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


            if (horizontalDirection != null && verticalDirection != null){
                double factor = 1 / Math.sqrt(2); // facteur de normalisation environ 70.7% de la vitesse

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

            // Check if the cat can move vertically and horizontally without colliding with an obstacle
            canMoveVertically = verticalDirection != null && !gameInstance.willCollideWithAnObstacle(verticalDirection.toString().toLowerCase(), speed, hitbox);
            canMoveHorizontally = horizontalDirection != null && !gameInstance.willCollideWithAnObstacle(horizontalDirection.toString().toLowerCase(), speed, hitbox);

            if (canMoveHorizontally){
                hitbox.setX(hitbox.getX() + currentSpeedX);
            }
            if (canMoveVertically){
                hitbox.setY(hitbox.getY() + currentSpeedY);
            }
        }

    }


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
            }
        });
    }
}
