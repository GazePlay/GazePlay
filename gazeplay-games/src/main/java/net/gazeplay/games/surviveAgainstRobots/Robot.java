package net.gazeplay.games.surviveAgainstRobots;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;

import java.util.Random;
/**
 * Represents a Robot object in the game "Survive Against Robots".
 */
public class Robot extends Rectangle {


    protected double speed;
    private final IGameContext gameContext;
    private final SurviveAgainstRobots gameInstance;
    private String directionHorizontal;
    private String directionVertical;
    protected boolean isDestroyed;
    private final boolean canShoot;
    private final double freqShoot;
    private final double bulletSpeed;
    private Random random;


    /**
     * Creates a new instance of the Robot class with the specified coordinates, dimensions, speed, game context, game instance, and shooting capability.
     * @param x the x-coordinate of the robot
     * @param y the y-coordinate of the robot
     * @param width the width of the robot
     * @param height the height of the robot
     * @param speed the speed of the robot
     * @param gameContext the game context to which the robot belongs
     * @param gameInstance the instance of the game
     * @param canShoot2 a boolean indicating whether the robot can shoot or not
     */
    public Robot(double x, double y, double width, double height, double speed, IGameContext gameContext, SurviveAgainstRobots gameInstance, boolean canShoot2) {
        super(x, y, width, height);
        this.speed = speed;
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.canShoot = canShoot2;
        this.isDestroyed = false;
        this.bulletSpeed = 5;
        this.freqShoot = 1.2;
        this.random = new Random();
        if (canShoot){
            ImagePattern robotShooter = new ImagePattern(new Image("data/surviveAgainstRobots/robots/Red.png"));
            this.setFill(robotShooter);
        }else{
            ImagePattern robotClassic = new ImagePattern(new Image("data/surviveAgainstRobots/robots/Orange.png"));
            this.setFill(robotClassic);
        }

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
                if (!isDestroyed){
                    if (canShoot){
                        if (nbframeShoot == 60*freqShoot){
                            nbframeShoot = 0;
                        }
                        if (nbframeShoot == 0){
                            automaticShoot();
                        }

                    }
                }

            }
        };

        robotAnimation.start();
    }


    /**
     * Moves the robot according to its current direction and speed, and checks for collisions with obstacles.
     * If the robot collides with an obstacle, it changes its direction accordingly.
     */
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


    /**
     * Generates a random direction for the robot to move in.
     * Sets the directionHorizontal and directionVertical fields accordingly.
     */
    private void generateDirection(){

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


    /**
     * Automatically shoots a bullet from the robot toward the player's hitbox.
     * The bullet is created at the center of the robot and added to the game context.
     * Then it starts moving toward the player's hitbox.
     */
    private void automaticShoot(){
        double x = this.getX() + this.getWidth() / 2;
        double y = this.getY() + this.getHeight() / 2;
        Bullet bullet = new Bullet(x, y, 10, 10, bulletSpeed,gameInstance, gameContext);
        bullet.setId("robotBullet");

        gameContext.getChildren().add(bullet);
        bullet.startMovingTowardTarget(gameInstance.player.hitbox);
    }
}
