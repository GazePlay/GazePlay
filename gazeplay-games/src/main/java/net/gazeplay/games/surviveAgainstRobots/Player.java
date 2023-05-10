package net.gazeplay.games.surviveAgainstRobots;

import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.animation.AnimationTimer;
import net.gazeplay.IGameContext;
import javafx.event.Event;
import javafx.event.EventHandler;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

public class Player extends Parent {

    protected Rectangle hitbox;
    private final double speed;
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
    private final double acceleration;


    protected AnimationTimer playerAnimationMovement;
    private final double freqShoot;
    private final double bulletSpeed;
    private Rectangle target;
    private final EventHandler<Event> enterEvent;

    public Player(Rectangle hitbox, double speed, IGameContext gameContext, SurviveAgainstRobots gameInstance, double freqShoot, Rectangle target) {
        this.hitbox = hitbox;
        this.speed = speed;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.acceleration = 0.5;
        this.freqShoot = freqShoot;


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
                    if (target==null){
                        playerMove();
                    }else{
                        playerMoveMouse();
                    }
                    nbframe++;
                    if (nbframe == 60*freqShoot){
                        nbframe = 0;
                    }
                    if (nbframe == 0){
                        if (target == null){
                            automaticShoot();
                        }else{
                            automaticShootMouse();
                            hitbox.toFront();
                        }
                    }
                }
            };
            playerAnimationMovement.start();
    }



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

    private void automaticShootMouse(){
        double x = this.hitbox.getX() + this.hitbox.getWidth() / 2;
        double y = this.hitbox.getY() + this.hitbox.getHeight() / 2;
        Bullet bullet = new Bullet(x, y, 10, 10, bulletSpeed,gameInstance, gameContext);
        bullet.setId("playerBullet");

        gameContext.getChildren().add(bullet);
        bullet.startMovingMouse(target);


    }
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

    private void playerMove(){

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
