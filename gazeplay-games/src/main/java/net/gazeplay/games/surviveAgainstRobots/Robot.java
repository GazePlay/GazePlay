package net.gazeplay.games.surviveAgainstRobots;

import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;

import java.util.Random;

public class Robot extends Rectangle {


    private final double speed;
    private final IGameContext gameContext;
    private final SurviveAgainstRobots gameInstance;
    /*private final String MOUVEMENT_UP = "up";
    private final String MOUVEMENT_DOWN = "down";
    private final String MOUVEMENT_RIGHT = "right";
    private final String MOUVEMENT_LEFT = "left";*/
    private String directionHorizontal;
    private String directionVertical;
    protected boolean isDestroyed;
    private boolean canShoot;
    private double freqShoot;
    private double bulletSpeed;
    public Robot(double x, double y, double width, double height, double speed, IGameContext gameContext, SurviveAgainstRobots gameInstance, boolean canShoot) {
        super(x, y, width, height);
        this.speed = speed;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.canShoot = canShoot;
        this.isDestroyed = false;
        this.bulletSpeed = 5;
        this.freqShoot = 1.2;

        this.setFill(Color.RED);
        AnimationTimer robotAnimation = new AnimationTimer() {

            int nbframes = 0;
            int nbframeShoot = 0;
            @Override
            public void handle(long now) {
                if (!isDestroyed){
                    moveRobot();
                }else{
                    stop();
                }
                if (nbframes == 120){
                    if (!isDestroyed){
                        generateDirection();
                    }
                    nbframes = 0;
                }
                nbframes++;

                nbframeShoot++;
                if (canShoot){
                    if (nbframeShoot == 60*freqShoot){
                        nbframeShoot = 0;
                    }
                    if (nbframeShoot == 0){
                        automaticShoot();
                    }

                }

            }
        };

        robotAnimation.start();
    }

    private void moveRobot(){
        if (!gameContext.getChildren().contains(this)){
            isDestroyed = true;
        }
        if (directionHorizontal == null && directionVertical == null && !isDestroyed){
            generateDirection();
        }

        double dirX = 0;
        double dirY = 0;
        boolean canMoveVertically;
        boolean canMoveHorizontally;



        if (directionHorizontal != null){
            if (directionHorizontal.compareToIgnoreCase("left") == 0){
                dirX = - speed;
            }else if (directionHorizontal.compareToIgnoreCase("right") == 0){
                dirX = speed;
            }else{
                dirX = 0;
            }
        }

        if (directionVertical != null){
            if (directionVertical.compareToIgnoreCase("up") == 0){
                dirY = - speed;
            }else if (directionVertical.compareToIgnoreCase("down") == 0){
                dirY = speed;
            }else{
                dirY = 0;
            }
        }

        if (directionHorizontal != null && directionVertical != null){
            double factor = 1 / Math.sqrt(2); // facteur de normalisation environ 70.7% de la vitesse

            if (dirX > 0){
                dirX = speed;
            }else{
                dirX = -speed;
            }

            if (dirY > 0){
                dirY = speed;
            }else{
                dirY = -speed;
            }

            dirX *= factor;
            dirY *= factor;
        }

        canMoveVertically = directionVertical != null && !gameInstance.willCollideWithAnObstacle(directionVertical, speed, this);
        canMoveHorizontally = directionHorizontal != null && !gameInstance.willCollideWithAnObstacle(directionHorizontal, speed, this);

        if (canMoveHorizontally){
            this.setX(this.getX() + dirX);
        }else{
            if (directionHorizontal != null && directionHorizontal.compareToIgnoreCase("left") == 0){
                directionHorizontal = "right";
            }else{
                directionHorizontal = "left";
            }
        }
        if (canMoveVertically){
            this.setY(this.getY() + dirY);
        }else{
            if (directionVertical != null && directionVertical.compareToIgnoreCase("up") == 0){
                directionVertical = "down";
            }else{
                directionVertical = "up";
            }
        }
    }


    private void generateDirection(){
        Random random = new Random();
        int dirHorizontal = random.nextInt(0,3);
        int dirVertical = random.nextInt(0,3);

        if (dirHorizontal == 1){
            directionHorizontal = "left";
        }else if (dirHorizontal == 2){
            directionHorizontal = "right";
        }else{
            directionHorizontal = null;
        }

        if (dirVertical == 1){
            directionVertical = "up";
        }else if (dirVertical == 2){
            directionVertical = "down";
        }else{
            directionVertical = null;
        }
    }

    private void automaticShoot(){
        double x = this.getX() + this.getWidth() / 2;
        double y = this.getY() + this.getHeight() / 2;
        Bullet bullet = new Bullet(x, y, 10, 10, bulletSpeed,gameInstance, gameContext);
        bullet.setId("robotBullet");

        gameContext.getChildren().add(bullet);
        bullet.startMovingMouse(gameInstance.player.hitbox);
    }
}
